package controllers

import akka.actor._
import akka.util.duration._

import play.api.mvc._
import play.api.libs.json._
import play.api.libs.iteratee._
import play.api.libs.concurrent._

import play.api.Play.current

import akka.util.Timeout
import akka.pattern.ask

import java.util.UUID
import models.Todo



object SocketIO extends Controller {

  implicit val timeout = Timeout(10 second)

  lazy val socketIOActor = {
    Akka.system.actorOf(Props[SocketIOActor])
  }

  def socketSetup(sessionId: String) = WebSocket.async[JsValue] {
    request =>
      (socketIOActor ? Join(sessionId)).asPromise.map {

        case Connected(enumerator) =>

          println("Connected")
          /* Create an Iteratee to consume the feed */
          val iteratee = Iteratee.foreach[JsValue] {
            cmd =>
              println("Talking -- " + cmd)
              socketIOActor ! Message(sessionId, cmd)
          }.mapDone {
            _ =>
              println("Quit!!!")
              socketIOActor ! Quit(sessionId)
          }

          (iteratee, enumerator)

        case CannotConnect(error) =>

          /* Connection error*/

          /* A finished Iteratee sending EOF */
          val iteratee = Done[JsValue, Unit]((), Input.EOF)

          /* Send an error and close the socket */
          val enumerator = Enumerator[JsValue](error).andThen(Enumerator.enumInput(Input.EOF))

          (iteratee, enumerator)

      }
  }

}

class SocketIOActor extends Actor {

  var sessions = Map.empty[String, PushEnumerator[JsValue]]
  /* Defining the receive method */
  def receive = {
    case Join(sessionId) => {
      println(sessionId)
      val channel = Enumerator.imperative[JsValue]()
      if (sessions.contains(sessionId)) {
        sender ! CannotConnect(Json.toJson(Map("error" -> "This username is already used")))
      } else {
        sessions = sessions + (sessionId -> channel)

        sender ! Connected(channel)
      }
    }
    case Message(sessionId,cmd) => {
      println(sessionId + "---" + cmd.toString())
      /* your message processing here! Like saving the data */

      /* fetching the command name from the event*/
      val name = (cmd \ "name").as[String]

      /* fetching userId for the command */
      val userId = (cmd \ "payload" \ "userId").as[String]

      /* processing at the back end based on the event name*/
      name match {
        /* in case of creating a new todo */
        case "createTodo" => {
          val uuid: UUID = java.util.UUID.randomUUID()
          val id = uuid.toString()
          val text = (cmd \ "payload" \ "text").as[String]
          val done = (cmd \ "payload" \ "done").as[Boolean]
          val disp_order = (cmd \ "payload" \ "disp_order").as[Int]
          Todo.create(Todo(id, text, done, disp_order,userId))
          println("Sending todo with id - " + id)
          notify(sessionId,Json.toJson(Map(
            "name"->Json.toJson("todoCreated"),
            "payload"->Json.toJson(Map(
              "id" -> Json.toJson(id),
              "text"->Json.toJson(text),
              "done"->Json.toJson(done),
              "disp_order"->Json.toJson(disp_order)
            ))
          )))
        }
        /* if the text of the todo is to be modified */
        case "changeTodoText" => {
          val id = (cmd \ "payload" \ "id").as[String]
          val text = (cmd \ "payload" \ "text").as[String]
          Todo.updateText(id, text,userId)
          println("Sending todo text with id - " + id)
          notify(sessionId, Json.toJson(Map(
            "name"->Json.toJson("todoChanged"),
            "payload"->Json.toJson(Map(
              "id" -> Json.toJson(id),
              "text"->Json.toJson(text)
            ))
          )))

        }
        /* if the status 'done' is to be changed */
        case "changeTodoStatus" => {
          val id = (cmd \ "payload" \ "id").as[String]
          val done = (cmd \ "payload" \ "done").as[Boolean]
          Todo.updateStatus(id, done,userId)
          println("Sending todo done with id - " + id)
          notify(sessionId, Json.toJson(Map(
            "name"->Json.toJson("todoChanged"),
            "payload"->Json.toJson(Map(
              "id" -> Json.toJson(id),
              "done"->Json.toJson(done)
            ))
          )))
        }

        /* if a todo is to be deleted */
        case "deleteTodo" => {
          val id = (cmd \ "payload" \ "id").as[String]
          Todo.delete(id,userId)
          println("Sending id - " + id)
          notify(sessionId, Json.toJson(Map(
            "name"->Json.toJson("todoDeleted"),
            "payload"->Json.toJson(Map(
              "id" -> Json.toJson(id)
            ))
          )))
        }

        /* for clear completed todos*/
        case "deleteDoneTodo" =>{
          val ids=(cmd\"payload"\"ids").as[Array[String]]

          println("in delete all completed")
          ids.foreach{id:String=>
            Todo.delete(id,userId)
            notify(sessionId, Json.toJson(Map(
              "name"->Json.toJson("todoDeleted"),
              "payload"->Json.toJson(Map(
                "id" -> Json.toJson(id)
              ))
            )))

          }

        }
      }
    }

    case Quit(sessionId) => {
      sessions = sessions - sessionId
      println(sessionId + "--- QUIT")
    }
  }

  def notify(sessionId: String, response: JsValue) {
    /*Sending data back here*/
    sessions(sessionId).push(response)
  }
}

case class Join(sessionId: String)

case class Message(sessionId: String, message: JsValue)

case class Quit(sessionId: String)

case class CannotConnect(message: JsValue)

case class Connected(enumerator: Enumerator[JsValue])