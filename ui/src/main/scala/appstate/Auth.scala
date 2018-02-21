package appstate

import diode.Action

// Model
case class Auth(jwt: Option[String] = None)

// Actions
case class Login(username: String, password: String) extends Action
case class UserLoggedIn(jwt: String) extends Action