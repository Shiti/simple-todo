/**
 * Created by IntelliJ IDEA.
 * User: shiti
 * Date: 5/12/12
 * Time: 2:22 AM
 * To change this template use File | Settings | File Templates.
 */

package models

import play.api.db._
import play.api.Play.current

import anorm._
import anorm.SqlParser._

case class Users(userId:String,password:String)

object Users {
  
  def simple={
    get[String]("userId")~
    get[String]("password")map{
      case userId~password=>Users(userId,password)
    }
  }

  def authenticate(userId:String,password:String):Option[Users]={
    DB.withConnection{implicit connection=>
      SQL("SELECT * FROM Users WHERE userId={userId} AND password={password}"
      ).on(
        'userId->userId,
        'password->password)
        .as(Users.simple.singleOpt)}
  }

  def find(userId: String): Option[Users] = {
    DB.withConnection { implicit connection =>
      SQL("SELECT * FROM Users WHERE userId = {userId}").on(
        'userId->userId
      ).as(Users.simple.singleOpt)
    }
  }

  def create(user:Users)={
    DB.withConnection{implicit connection=>
      SQL("INSERT INTO Users(userId,password) VALUES({userId},{password})").on(
        'userId->user.userId,
        'password->user.password
      ).executeUpdate()
    }
  }

  def newUser(userId:String,password:String):Boolean={
    if (find(userId).isEmpty)
    {
      val user=Users(userId,password)
      create(user)
      true
    }
    else
      false
  }
  
}
