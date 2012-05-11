package controllers

import play.api._
import libs.iteratee.{Enumerator, Iteratee}
import libs.json.{JsObject, JsValue, Json}
import play.api.mvc._

import com.codahale.jerkson.Json._

import play.api.data._
import play.api.data.Forms._

import views._
import models.Todo

object Application extends Controller {

  def index = Action { implicit request =>
    Ok(views.html.index("Things To do"))
  }

  def login=Action{implicit request=>
    Ok(views.html.login(loginForm))
  }

  /* GET all todos from backend*/
  def allTodos= Action {
    val s=Todo.getAll()
    Ok(generate(s))
  }

  /* Login form */
  val loginForm=Form(
    tuple(
      "userId"->nonEmptyText,
      "password"->nonEmptyText
    )
  )

  
}