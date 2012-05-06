/**
 * Created by IntelliJ IDEA.
 * User: shiti
 * Date: 3/15/12
 * Time: 9:16 PM
 * To change this template use File | Settings | File Templates.
 */

window.Model=new Object();

/* Todo Model */
/* Our basic **Todo** model has `text`, `order`, and `done` attributes. */

Model.Todo = Backbone.Model.extend({
    /* Default attributes for a todo item. */
    defaults: function() {
        return {
            modelName:'todo',  /* so denormalizers can resolve events to model */
            done:  false,
            disp_order: Model.Todos.nextOrder()
        };
    },
    /* bind this model to get event updates */
    initialize:function(){
        this.bindCQRS();
    },

    /* Toggle the `done` state of this todo item. */
    toggle: function() {
        this.save({done: !this.get("done")});
    }

});

/* Todo Collection */

Model.TodoList = Backbone.Collection.extend({

    url: "/bb/todos",

    /* Reference to this collection's model. */
    model: Model.Todo,

    /* Filter down the list of all todo items that are finished. */
    done: function() {
        return this.filter(function(todo){ return todo.get('done'); });
    },

    /* Filter down the list to only todo items that are still not finished. */
    remaining: function() {
        return this.without.apply(this, this.done());
    },

    /* We keep the Todos in sequential order, despite being saved by unordered
     GUID in the database. This generates the next order number for new items.*/
    nextOrder: function() {
        if (!this.length)
            return 1;
        return this.last().get('disp_order') + 1;
    },

    /* Todos are sorted by their original insertion order. */
    comparator: function(todo) {
        return todo.get('disp_order');
    }

});

