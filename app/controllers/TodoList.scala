/**
 * Created by IntelliJ IDEA.
 * User: shiti
 * Date: 5/12/12
 * Time: 11:14 PM
 * To change this template use File | Settings | File Templates.
 */

package controllers

import play.api.mvc._
import com.codahale.jerkson.Json._
import views._

import models._

object TodoList extends Controller with Secured{

//  def index = Action { implicit request =>
//    Ok(views.html.index("Things To do"))
//  }

  def index = IsAuthenticated { username => _ =>
    Users.find(username).map { user =>
      Ok(html.index("Things to Do"))
    }.getOrElse(Forbidden)
  }

  /* GET all todos from backend*/
  def allTodos= Action {implicit request=>
    session.get("connected").map{userId=>
      println("its"+userId)
      val s=Todo.getAll(userId)
      Ok(generate(s))
    }.getOrElse{
      Unauthorized("Oops!!!")
    }

  }

}
