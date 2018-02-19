package utils

import org.scalajs.dom.ext.Ajax

//TODO move some stuff to config
object ApiMiddleware {
  def Post(url: String, payload: Ajax.InputData) = {
    Ajax.post(
      url = url, 
      data = payload, 
      timeout = 9000, 
      headers = Map("Content-type" -> "application/json"), 
      withCredentials = false, 
      responseType = "text")
  }
  
  def Get(url: String) = {
    Ajax.get(
      url = url, 
      data = null, 
      timeout = 9000, 
      headers = Map.empty, 
      withCredentials = false, 
      responseType = "text")
  }
  
  def Delete(url: String, payload: Ajax.InputData) = {  
    Ajax.delete(
      url = url, 
      data = payload, 
      timeout = 9000, 
      headers = Map("Content-type" -> "application/json"), 
      withCredentials = false, 
      responseType = "text")
  }

  def Put(url: String, payload: Ajax.InputData) = {
    Ajax.put(
      url = url, 
      data = payload, 
      timeout = 9000, 
      headers = Map("Content-type" -> "application/json"), 
      withCredentials = false, 
      responseType = "text")
  }
}