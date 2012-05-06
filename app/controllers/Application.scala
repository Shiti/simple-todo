package controllers

import play.api._
import libs.iteratee.{Enumerator, Iteratee}
import libs.json.{JsObject, JsValue, Json}
import play.api.mvc._

import com.codahale.jerkson.Json._

import models.Todo

object Application extends Controller {

  def index = Action { implicit request =>
    Ok(views.html.index("Things To do"))
  }

  /* GET all*/
  def allTodos= Action {
    val s=Todo.getAll()
    Ok(generate(s))
    }

  
}