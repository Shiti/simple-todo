/**
 * Created by IntelliJ IDEA.
 * User: shiti
 * Date: 3/15/12
 * Time: 9:16 PM
 * To change this template use File | Settings | File Templates.
 */

window.Model=new Object();

/* Todo Model */
/* Our basic **Todo** model has `text`, `disp_order`, and `done` attributes. */

Model.Todo = Backbone.Model.extend({
    /* Default attributes for a todo item. */
    defaults: function() {
        return {

            done:  false,
            disp_order: Model.Todos.nextOrder()
        };
    },

    modelName:'todo',  /* so denormalizers can resolve events to model */

    /* bind this model to get event updates */
    initialize:function(){
        this.bindCQRS();
    }

});

