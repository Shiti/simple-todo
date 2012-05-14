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

  def index = IsAuthenticated { username => _ =>
    Users.find(username).map { user =>
      Ok(html.index("Things to Do",username))
    }.getOrElse(Forbidden)
  }

  /* GET all todos from backend*/
  def allTodos=IsAuthenticated { username => _ =>
    Users.find(username).map { user =>
      val s=Todo.getAll(user.userId)
      Ok(generate(s))
    }.getOrElse{
      Unauthorized("Oops!!!")
    }

  }

}
