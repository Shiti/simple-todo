package controllers

import play.api._
import libs.json.JsValue
import play.api.mvc._
import play.api.libs.json.Json
import java.util.UUID

import com.codahale.jerkson.Json._


import models.Todo

object Application extends Controller {

  def index = Action {
    Ok(views.html.index())
  }

  /* GET */
  def getTodo(id:String) = Action(parse.json) {request =>
    (request.body).asOpt[JsValue].map{ model =>
      println("gettodo:" + model.toString())
      Todo.findById((model\"id").toString())
      Ok("get"+model.toString())
    }.getOrElse {
      BadRequest("Missing parameter [model]")
    }

  }

  /* POST */
  def postTodo = Action(parse.json) {request =>
    (request.body).asOpt[JsValue].map { model =>
      println("postTodo:" + model.toString())

      val uuid:UUID = java.util.UUID.randomUUID()
      val us = uuid.toString()

      var jsVal = Json.parse(model.toString())
      Todo.create(Todo(us,(model\"text").toString(),(model\"done").toString(),(model\"disp_order").as[Int]))

      Ok(Json.parse("{\"id\":\""+us+"\"}").toString)
    }.getOrElse {
      BadRequest("Missing parameter [model]")
    }

  }

  /* PUT */
  def putTodo(id:String) = Action(parse.json) {request =>
    (request.body).asOpt[JsValue].map{ model =>
      val t=Todo.findById(id)
      val text=(model\"text")
      if(t.get.text!=text){
        Todo.updateText(id,text.toString())
        val map = Map("text" -> text)
        println(map)
        val json:JsValue = Json.toJson(map)
        println(json)
        Ok(json.toString)
      }
      else{
        val done=(model\"done")
        Todo.updateStatus(id,done.toString())
        Ok(Json.parse("{\"done\":\""+done+"\"}").toString)
      }
    }.getOrElse {
      BadRequest("Missing parameter [model]")
    }
  }

  /* DELETE */
  def deleteTodo(id:String) = Action {
    val model=Todo.findById(id)
    Todo.delete(id)
    Ok(Json.toJson(generate(model)))
  }

  /* GET */
  def allTodos= Action {
    val s=generate(Todo.getAll())
    Ok(Json.toJson(s))
    }

}