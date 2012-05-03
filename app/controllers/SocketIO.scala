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

import models.Todo._
import java.util.UUID
import models.Todo


object SocketIO extends Controller {

  implicit val timeout = Timeout(10 second)

  lazy val socketIOActor = {
    Akka.system.actorOf(Props[SocketIOActor])
  }

  /* def init = Action {
    val sessionId = java.util.UUID.randomUUID().toString()
    println(sessionId)
    Ok(sessionId + ":1:10:websocket")
  } */

  def socketSetup(sessionId: String) = WebSocket.async[JsValue] {
    request =>
      (socketIOActor ? Join(sessionId)).asPromise.map {

        case Connected(enumerator) =>

          println("Connected")
          // Create an Iteratee to consume the feed
          val iteratee = Iteratee.foreach[JsValue] {
            event =>
              println("Talking -- " + event)
              socketIOActor ! Message(sessionId, event)
          }.mapDone {
            _ =>
              println("Quit!!!")
              socketIOActor ! Quit(sessionId)
          }

          (iteratee, enumerator)

        case CannotConnect(error) =>

          // Connection error

          // A finished Iteratee sending EOF
          val iteratee = Done[JsValue, Unit]((), Input.EOF)

          // Send an error and close the socket
          val enumerator = Enumerator[JsValue](error).andThen(Enumerator.enumInput(Input.EOF))

          (iteratee, enumerator)

      }
  }

}

class SocketIOActor extends Actor {

  var sessions = Map.empty[String, PushEnumerator[JsValue]]

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
    case Message(sessionId, event) => {
      println(sessionId + "---" + event)
      //DO your message processing here! Like saving the data
      //      val id = math.round(math.random * 1000)
      val name = (event \ "name").as[String];
      var id = (event \ "payload" \ "id").as[String]

      name match {
        case "todoCreated" => {
          val uuid: UUID = java.util.UUID.randomUUID()
          id = uuid.toString()
          val text = (event \ "payload" \ "text").as[String]
          val done = (event \ "payload" \ "done").as[Boolean]
          val disp_order = (event \ "payload" \ "disp_order").as[Int]
          Todo.create(Todo(id, text, done, disp_order))
        }
        case "todoTextChanged" => {
          //          val id=(event\"payload"\"id").as[String]
          val text = (event \ "payload" \ "text").as[String]
          Todo.updateText(id, text)

        }
        case "todoStatusChanged" => {
          //          val id=(event\"payload"\"id").as[String]
          val done = (event \ "payload" \ "done").as[Boolean]
          Todo.updateStatus(id, done)
        }

        case "todoDeleted" => {
          Todo.delete(id)
        }
      }

      println("Sending ID - " + id)
      notify(sessionId, Json.toJson(Map("id" -> id)))

    }

    case Quit(sessionId) => {
      sessions = sessions - sessionId
      println(sessionId + "--- QUIT")
    }
  }

  def notify(sessionId: String, response: JsValue) {
    //Sending data back here
    sessions(sessionId).push(response)
  }
}

case class Join(sessionId: String)

case class Heartbeat(sessionId: String)

case class Message(sessionId: String, message: JsValue)

case class Quit(sessionId: String)

case class CannotConnect(message: JsValue)

case class Connected(enumerator: Enumerator[JsValue])