/**
 * Created by IntelliJ IDEA.
 * User: shiti
 * Date: 4/1/12
 * Time: 12:54 AM
 * To change this template use File | Settings | File Templates.
 */

package models


import play.api.db._
import play.api.Play.current

import anorm._
import anorm.SqlParser._
import play.api.libs.json.JsValue


case class Todo(id:String,text:String,done:Boolean,disp_order:Int,userId:String)

object Todo {

   var userId:String=null

  /* Parse a todo from the result set */
  def simple={
    get[String]("id")~
    get[String]("text")~
    get[Boolean]("done")~
    get[Int]("disp_order")~
    get[String]("userId")map {
      case id~text~done~disp_order~userId =>Todo(id,text,done,disp_order,userId)
    }
  }

  /* Retrieve all todos */
  def getAll(userId:String):Seq[Todo]={
    this.userId=userId
    DB.withConnection{implicit connection=>
      SQL("SELECT * FROM Todo WHERE userId={userId}").on('userId->userId).as(Todo.simple *)
    }
  }

  /* Retrieve a todo by its id --this is not needed for our app */
  def findById(id: String): Option[Todo] = {
    DB.withConnection { implicit connection =>
      SQL("select * from Todo where id = {id}").on(
        'id -> id
      ).as(Todo.simple.singleOpt)
    }
  }

  /* Add a todo to the database */
  def create(task:Todo)={
    if (this.userId==task.userId){
      DB.withConnection{implicit connection=>
        SQL("INSERT INTO Todo (id,text,done,disp_order,userId) VALUES({id},{text},{done},{disp_order},{userId})").on(
          'id->task.id,
          'text->task.text,
          'done->task.done,
          'disp_order->task.disp_order,
          'userId->task.userId
        ).executeUpdate()
      }
    }
  }

  /* Delete a todo by its id */
  def delete(id:String,userId:String)={
    if (this.userId==userId){
      DB.withConnection{implicit connection=>
        SQL("DELETE FROM Todo WHERE id={id} AND userId={userId}").on(
          'id->id,
          'userId->userId
        ).executeUpdate()
      }
    }
  }

  /* Update a todo text */
  def updateText(id:String,text:String,userId:String)={
    if (this.userId==userId){
      DB.withConnection{implicit connection=>
        SQL("UPDATE Todo SET text={text} WHERE id={id} AND userId={userId}").on(
          'id->id,
          'text->text,
          'userId->userId
        ).executeUpdate()
      }
    }
  }

  /* Update a todo done attribute */
  def updateStatus(id:String,done:Boolean,userId:String)={
    if (this.userId==userId){
      DB.withConnection{implicit connection=>
        SQL("UPDATE Todo SET done={done} WHERE id={id} AND userId={userId}").on(
          'id->id,
          'done->done,
          'userId->userId
        ).executeUpdate()
      }
    }
  }

  /* Delete all completed todo */
  def deleteDone()={
    DB.withConnection{implicit connection=>
      SQL("DELETE FROM Todo WHERE done=true").executeUpdate()
    }
  }

};