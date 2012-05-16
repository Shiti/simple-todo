/**
 * Created by IntelliJ IDEA.
 * User: shiti
 * Date: 3/15/12
 * Time: 9:30 PM
 * To change this template use File | Settings | File Templates.
 */

/* global variable for websocket */
var todoSocket;

$(function () {
    /* websocket */
    var WS = window['MozWebSocket'] ? MozWebSocket : WebSocket;
    todoSocket = new WS(window.wsBaseUrl);

    /* Wire up communication to/from server -forward events from the server via websocket*/
    todoSocket.onmessage = function (msg) {
        receiveTodoEvent(JSON.parse(msg.data));
    }

    function receiveTodoEvent(msg) {
        /* using backbone.cqrs event denormalizers */
        Backbone.CQRS.hub.emit('events', msg);
    }

    /* The Application */

    /* Our overall **AppView** is the top-level piece of UI.*/
    View.AppView = Backbone.View.extend({

        /* Instead of generating a new element, bind to the existing skeleton of
        * the App already present in the HTML. */
        el:$("#todoapp"),

        /* Delegated events for creating new items, and clearing completed ones.  */
        events:{
            "keypress #new-todo":"createOnEnter",
            "keyup #new-todo":"showTooltip",
            "click .todo-clear a":"clearCompleted"
        },

        /* At initialization we bind to the relevant events on the `Todos`
        * collection, when items are added or changed. Kick things off by
        * loading any preexisting todos that might be saved in backend */
        initialize:function () {
            this.input = this.$("#new-todo");

            Model.Todos.bind('add', this.addOne, this);
            Model.Todos.bind('reset', this.addAll, this);
            Model.Todos.bind('all', this.render, this);  /* binding rendering of the stats footer*/
            Model.Todos.bind('remove', this.removeItem ,this);

            Model.Todos.fetch();
        },

        /* Remove a todo from the frontend once it is removed from the collection*/
        removeItem: function(model){
            $('#' + model.id).parent("li").remove();
        },

        /* Re-rendering the App just means refreshing the statistics -- the rest
        * of the app doesn't change.
        * render function using mustache.js  */
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

        /* Add a single todo item to the list by creating a view for it, and
        * appending its element to the `<ul>`.*/
        addOne:function (todo) {
            var view = new View.TodoView({model:todo});
            $("#todo-list").append(view.render().el);
        },

        /* Add all items in the **Todos** collection at once.*/
        addAll:function () {
            Model.Todos.each(this.addOne);
        },

        /* If you hit return in the main input field, and there is text to save,
        * create new **Todo** model persisting it to backend. */
        createOnEnter:function (e) {
            var text = this.input.val();
            if (!text || e.keyCode != 13) return;
            var t = new Model.Todo({text:text});

            /* CQRS command */
            var cmd = new Backbone.CQRS.Command({
                name:"createTodo",
                payload:{
                    text:text,
                    done:false,
                    disp_order:Model.Todos.nextOrder(),
                    userId:window.session.getUserId()
                }
            });
            /* emit it */
            cmd.emit();

            /* resetting the input box*/
            this.input.val('');
        },

        /* Clear all done todo items, destroying their models. */
        clearCompleted:function () {
            /* using array in order to send the ids of completed todo to the server*/
            var finished=[];
            _.each(Model.Todos.done(), function(todo){
                finished.push(todo.id);
            });

            /* CQRS command */
            var cmd = new Backbone.CQRS.Command({
                name:"deleteDoneTodo",
                payload:{
                    ids:finished,
                    userId:window.session.getUserId()

                }
            });
            /* emit it */
            cmd.emit();
        },

        /* Lazily show the tooltip that tells you to press `enter` to save
        * a new todo item, after one second. */
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

    /* Create our global collection of **Todos**. */
    Model.Todos = new Model.TodoList;

    window.TodoApp = new View.AppView;

    /* Init Backbone.CQRS */
    Backbone.CQRS.hub.init();

    /* override Backbone.sync with CQRS.sync which allows only GET method */
    Backbone.sync = Backbone.CQRS.sync;

    /* Wire up communication to server -forward commands to server via websocket */
    Backbone.CQRS.hub.on('commands', function (cmd) {
        /* pass commands to websocket */
        todoSocket.send(JSON.stringify(cmd));
    });


    /* Create a few EventDenormalizers
    * the forModel must be the modelName specified in the model for binding*/

    /* todoCreated event (change methode to create and pass in model and collection to add it to */
    var todoCreateHandler = new Backbone.CQRS.EventDenormalizer({
        methode:'create',
        model:Model.Todo,
        collection:Model.Todos,

        /* bindings */
        forModel:'todo',
        forEvent:'todoCreated'
    });

    /* todoChanged event (just go with defaults)- by default the methode will be set to update
    * in case of both status and text change */
    var todoChangedHandler = new Backbone.CQRS.EventDenormalizer({

        forModel:'todo',
        forEvent:'todoChanged'
    });

    /* todoDeleted event (just change methode to delete) */
    var todoDeletedHandler = new Backbone.CQRS.EventDenormalizer({
        methode:'delete',

        /* bindings */
        forModel:'todo',
        forEvent:'todoDeleted'
    });

});
