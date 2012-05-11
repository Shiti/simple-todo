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

  def getPassword(userId:String):Option[String]={
    DB.withConnection{implicit connection=>
      SQL("SELECT password FROM Users WHERE userId={userId}").on(
        'userId->userId
      ).as(scalar[String].singleOpt)
    }
  }

  def authenticate(userId:String,password:String):Boolean={
    (password==getPassword(userId))
  }
}
