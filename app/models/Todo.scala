/**
 * Created by IntelliJ IDEA.
 * User: shiti
 * Date: 4/1/12
 * Time: 12:54 AM
 * To change this template use File | Settings | File Templates.
 */

package models

import java.util.UUID

import play.api.db._
import play.api.Play.current

import anorm._
import anorm.SqlParser._
import play.api.libs.json.JsValue


case class Todo(id:String,text:String,done:Boolean,disp_order:Int)

object Todo {

  /* Parse a todo from the result set */

  def simple={
    get[String]("id")~
    get[String]("text")~
    get[Boolean]("done")~
    get[Int]("disp_order")map {
      case id~text~done~disp_order =>Todo(id,text,done,disp_order)
    }
  }
//  def simple={
//    get[String]("id")~
//      get[String]("text")~
//      get[String]("done") map {
//      case id~text~done =>Todo(id,text,done)
//    }
//  }

  /* Retrieve all todos */
  def getAll():Seq[Todo]={
    DB.withConnection{implicit connection=>
      SQL("SELECT * FROM Todo").as(Todo.simple *)

    }

  }

  /* Retrieve a todo by its id */
  /* this is not needed for our app */
  def findById(id: String): Option[Todo] = {
    DB.withConnection { implicit connection =>
      SQL("select * from Todo where id = {id}").on(
        'id -> id
      ).as(Todo.simple.singleOpt)
    }
  }

  /* Add a todo to the database */
  def create(task:Todo)={
    DB.withConnection{implicit connection=>
      SQL("INSERT INTO Todo (id,text,done,disp_order) VALUES({id},{text},{done},{disp_order})").on(
        'id->task.id,
        'text->task.text,
        'done->task.done,
        'disp_order->task.disp_order
      ).executeUpdate()
    }
  }

  /* Delete a todo by its id */
  def delete(id:String)={
    DB.withConnection{implicit connection=>
      SQL("DELETE FROM Todo WHERE id={id}").on(
        'id->id).executeUpdate()

    }
  }

  /* Update a todo text */
  def updateText(id:String,text:String)={
    DB.withConnection{implicit connection=>
      SQL("UPDATE Todo SET text={text} WHERE id={id}").on(
        'id->id,'text->text
      ).executeUpdate()
    }
  }

  /* Update a todo done attribute */
  def updateStatus(id:String,done:Boolean)={
    DB.withConnection{implicit connection=>
      SQL("UPDATE Todo SET done={done} WHERE id={id}").on(
        'id->id,'done->done
      ).executeUpdate()
    }
  }

  def handler(s:JsValue)={
    val name=(s\"name").as[String];
    println(name)
    name match {
      case "createTodo" =>{
        val uuid:UUID = java.util.UUID.randomUUID()
        val us = uuid.toString()
        val text=(s\\"payload" \ "text").as[String]
        val done=(s\\"payload"\"done").as[Boolean]
        val disp_order=(s\\"payload"\"disp_order").as[Int]
        println(text+';'+done)
        this.create(Todo(us,text,done,disp_order))
        println("created")

      }
      case "changeTodoText" =>{
        val id=(s\\"payload"\"id").as[String]
        val text=(s\\"payload"\"text").as[String]
        this.updateText(id,text)

      }
      case "changeTodoStatus" =>{
        val id=(s\\"payload"\"id").as[String]
        val done=(s\\"payload"\"done").as[Boolean]
        this.updateStatus(id,done)
      }

      case "deleteTodo" =>{
        val id=(s\\"payload"\"id").as[String]
        this.delete(id)
      }
    }
  }


}