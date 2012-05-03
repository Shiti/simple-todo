/**
 * Created by IntelliJ IDEA.
 * User: shiti
 * Date: 3/15/12
 * Time: 9:30 PM
 * To change this template use File | Settings | File Templates.
 */

var todoSocket;

$(function () {
    var WS = window['MozWebSocket'] ? MozWebSocket : WebSocket;
//    todoSocket = new WS("ws://"+location.host+"/handleTodos");
    todoSocket = new WS(window.wsBaseUrl);

    todoSocket.onmessage = function (msg) {
        console.log(msg);
        receiveTodoEvent(msg.data);
    }

    function receiveTodoEvent(msg) {
        Backbone.CQRS.hub.emit('events', msg)
    }

//window.App=new Object();
    // The Application
    // ---------------

    // Our overall **AppView** is the top-level piece of UI.
    View.AppView = Backbone.View.extend({

        // Instead of generating a new element, bind to the existing skeleton of
        // the App already present in the HTML.
        el:$("#todoapp"),

        // Our template for the line of statistics at the bottom of the app.
//        statsTemplate: _.template($('#stats-template').html()),

        // Delegated events for creating new items, and clearing completed ones.
        events:{
            "keypress #new-todo":"createOnEnter",
            "keyup #new-todo":"showTooltip",
            "click .todo-clear a":"clearCompleted"
        },

        // At initialization we bind to the relevant events on the `Todos`
        // collection, when items are added or changed. Kick things off by
        // loading any preexisting todos that might be saved in *localStorage*.
        initialize:function () {
            this.input = this.$("#new-todo");

            Model.Todos.bind('add', this.addOne, this);
            Model.Todos.bind('reset', this.addAll, this);
            Model.Todos.bind('all', this.render, this);

            Model.Todos.fetch();
        },

        // Re-rendering the App just means refreshing the statistics -- the rest
        // of the app doesn't change.
//        render: function() {
//            this.$('#todo-stats').html(this.statsTemplate({
//                total:      Model.Todos.length,
//                done:       Model.Todos.done().length,
//                remaining:  Model.Todos.remaining().length
//            }));
//        },
        //render function using mustache.js
        render:function () {
            var stats = {
                total:Model.Todos.length,
                done:Model.Todos.done().length,
                remaining:Model.Todos.remaining().length,
                term:(Model.Todos.remaining().length == 1) ? 'item' : 'items',
                term1:(Model.Todos.done().length == 1) ? 'item' : 'items',
                doneList:(Model.Todos.done().length > 0),
                totalList:(Model.Todos.length > 0)

            };
            var stats_template = $('#stats-template').html();

            var html = Mustache.to_html(stats_template, stats);

            this.$('#todo-stats').html(html);

        },

        // Add a single todo item to the list by creating a view for it, and
        // appending its element to the `<ul>`.
        addOne:function (todo) {
            var view = new View.TodoView({model:todo});
            $("#todo-list").append(view.render().el);
        },

        // Add all items in the **Todos** collection at once.
        addAll:function () {
            Model.Todos.each(this.addOne);
        },

        // If you hit return in the main input field, and there is text to save,
        // create new **Todo** model persisting it to *localStorage*.
        createOnEnter:function (e) {
            var text = this.input.val();
            if (!text || e.keyCode != 13) return;
            var t = new Model.Todo({text:text});
            Model.Todos.add(t);
            this.input.val('');

            //CQRS command
            var cmd = new Backbone.CQRS.Command({
                name:'createTodo',
                payload:{
                    text:text,
                    done:false,
                    disp_order:Model.Todos.nextOrder()
                }
            });
            //emit it
            cmd.emit();
        },

        // Clear all done todo items, destroying their models.
        clearCompleted:function () {
            _.each(Model.Todos.done(), function (todo) {
                todo.destroy();
            });
            return false;
        },

        // Lazily show the tooltip that tells you to press `enter` to save
        // a new todo item, after one second.
        showTooltip:function (e) {
            var tooltip = this.$(".ui-tooltip-top");
            var val = this.input.val();
            tooltip.fadeOut();
            if (this.tooltipTimeout) clearTimeout(this.tooltipTimeout);
            if (val == '' || val == this.input.attr('placeholder')) return;
            var show = function () {
                tooltip.show().fadeIn();
            };
            this.tooltipTimeout = _.delay(show, 1000);
        }

    });

    // Create our global collection of **Todos**.
    Model.Todos = new Model.TodoList;

    window.TodoApp = new View.AppView;

    // Init Backbone.CQRS
    // ------------------
    Backbone.CQRS.hub.init();

    // override Backbone.sync with CQRS.sync which allows only GET method
    Backbone.sync = Backbone.CQRS.sync;


    // Wire up communication to/from server
    // ------------------------------------

    // pass in events from your websocket
    //todoSocket.onmessage=receiveEvent();

    // forward commands to server via websocket

    Backbone.CQRS.hub.on('commands', function (cmd) {
        var evt = cmd;

        // convert command to event
        if (evt.name === 'createTodo') {
            evt.name = 'todoCreated';
            evt.payload.id = _.uniqueId('p'); // add a id on simulated 'serverside'
        } else if (evt.name === 'changeTodoText') {
            evt.name = 'todoTextChanged';
        } else if (evt.name === 'changeTodoStatus') {
            evt.name = 'todoStatusChanged';
        } else if (evt.name === 'deleteTodo') {
            evt.name = 'todoDeleted';
        }

        // send with some delay - better to see effect
//        setTimeout(function() {
        // todoSocket.emit('events', evt);    // pass commands to socket
        todoSocket.send(JSON.stringify(cmd));
//        }, delay);
    });


    // Create a few EventDenormalizers
    // -------------------------------

    // todoCreated event (change methode to create and pass in model and collection to add it to
    var todoCreateHandler = new Backbone.CQRS.EventDenormalizer({
        methode:'create',
        model:Model.Todo,
        collection:Model.TodoList,

        // bindings
        forModel:'Model.Todo',
        forEvent:'todoCreated'
    });

    // todoChanged event (just go with defaults)
    var todoChangedHandler = new Backbone.CQRS.EventDenormalizer({
        forModel:'Model.Todo',
        forEvent:'todoChanged'
    });

    // todoDeleted event (just change methode to delete)
    var todoDeletedHandler = new Backbone.CQRS.EventDenormalizer({
        methode:'delete',

        // bindings
        forModel:'Model.Todo',
        forEvent:'todoDeleted'
    });
});
