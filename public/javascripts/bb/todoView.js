/**
 * Created by IntelliJ IDEA.
 * User: shiti
 * Date: 3/15/12
 * Time: 9:26 PM
 * To change this template use File | Settings | File Templates.
 */


window.View=new Object();

/* Todo Item View */

/* The DOM element for a todo item... */
View.TodoView = Backbone.View.extend({

    /*... is a list tag. */
    tagName:  "li",

    /* The DOM events specific to an item. */
    events: {
        "click .check"              : "toggleDone",
        "dblclick div.todo-text"    : "edit",
        "click span.todo-destroy"   : "clear",
        "keypress .todo-input"      : "updateOnEnter"
    },

    /* The TodoView listens for changes to its model, re-rendering. */
    initialize: function() {
        this.model.bind('change', this.render, this);
    },

    /* Re-render the contents of the todo item. */
    render: function() {

        var textTemplate=$("#item-template").html();

        var modelData=this.model.toJSON();

        var check_stats={
            checkIfDone:modelData.done?'done':'' ,
            tickMarked:modelData.done?'checked="checked" ':''

        };

        var dataSet=$.extend(check_stats,modelData);
        $(this.el).html(Mustache.to_html(textTemplate,dataSet));
        this.setText();
        return this;
    },

    /* To avoid XSS (not that it would be harmful in this particular app),
    * we use `jQuery.text` to set the contents of the todo item.*/
    setText: function() {
        var text = this.model.get('text');
        this.$('.todo-text').text(text);
        this.input = this.$('.todo-input');
        this.input.bind('blur', _.bind(this.close, this)).val(text);
    },

    /* Toggle the `"done"` state of the model. */
    toggleDone: function() {

        /* CQRS command */
        var cmd=new Backbone.CQRS.Command({
            name:"changeTodoStatus",
            payload:{
                id:this.model.id,
                done:!this.model.get('done')
            }
        });

        /* emit it */
        cmd.emit();
    },

    /* Switch this view into `"editing"` mode, displaying the input field. */
    edit: function() {
        $(this.el).addClass("editing");
        this.input.focus();
    },

    /* Close the `"editing"` mode, saving changes to the todo. */
    close: function() {
        var newText=this.input.val();
        if (newText!=this.model.get('text')) {

            /* CQRS command */
            var cmd = new Backbone.CQRS.Command({
                name: "changeTodoText",
                payload: {
                    id: this.model.id,
                    text: newText
                }
            });

            /* emit it */
            cmd.emit();
        }
        $(this.el).removeClass("editing");
    },

    /* If you hit `enter`, we're through editing the item.*/
    updateOnEnter: function(e) {
        if (e.keyCode == 13) this.close();
    },

    /* Remove the item, destroy the model.*/
    clear: function(e) {
        e.preventDefault();

        /* CQRS command */
        var cmd = new Backbone.CQRS.Command({
            name: "deleteTodo",
            payload: {
                id: this.model.id
            }
        });

        /* emit it */
        cmd.emit();
    }

});

