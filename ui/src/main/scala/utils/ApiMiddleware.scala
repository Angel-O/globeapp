package utils

import org.scalajs.dom.ext.Ajax
import appstate.{AppModel, AppCircuit, Connect}

//TODO move some stuff to config
//TODO store jwt in local or session storage or cookie
object ApiMiddleware extends Connect { 
  
  private var token: String = ""
  connect()(AppCircuit.authSelector, token = AppCircuit.authSelector.value.jwt.getOrElse(""))

  val contentHeader = ("Content-type" -> "application/json")
  val headers: Map[String, String] = Map.empty
  def setHeader = (header: (String, String)) => {
    header._2 match {
      case "" => headers
      case _ => headers + header
    }
  }
  def Get(url: String) = {
    Ajax.get(
      url = url, 
      data = null, 
      timeout = 9000, 
      headers = setHeader(("Token" -> token)), 
      withCredentials = false, 
      responseType = "text")
  }

  def Post(url: String, payload: Ajax.InputData) = {
    Ajax.post(
      url = url, 
      data = payload, 
      timeout = 9000, 
      headers = setHeader(contentHeader) ++ setHeader(("Token" -> token)), 
      withCredentials = false, 
      responseType = "text")
  }
  
  def Delete(url: String, payload: Ajax.InputData) = {  
    Ajax.delete(
      url = url, 
      data = payload, 
      timeout = 9000, 
      headers = setHeader(contentHeader) ++ setHeader(("Token" -> token)), 
      withCredentials = false, 
      responseType = "text")
  }

  def Put(url: String, payload: Ajax.InputData) = {
    Ajax.put(
      url = url, 
      data = payload, 
      timeout = 9000, 
      headers = setHeader(contentHeader) ++ setHeader(("Token" -> token)), 
      withCredentials = false, 
      responseType = "text")
  }
}