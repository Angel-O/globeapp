package app

import diode.Circuit
import diode.ActionHandler
import diode.ModelRW
import apimodels.User
import components.Components.Implicits.{log, toBindingSeq}
import com.thoughtworks.binding.Binding.Vars
import com.thoughtworks.binding.Binding.BindingSeq
import com.thoughtworks.binding.dom
import org.scalajs.dom.ext.Ajax

object AppCircuit extends Circuit[AppModel] {
  
  def initialModel = AppModel(Users(), Cars())
  
  def defaultSelector[M,T]: ModelRW[M,T] = zoomTo(x => x.self).asInstanceOf[ModelRW[M,T]]
  val userSelector = zoomTo(x => x.users.users)
  val carSelector = zoomTo(x => x.cars.cars)
  
  // Using fold handlers rather than compose handlers to
  // allow all handlers to process the actions without stopping
  // soon as the the action has been handled
  override val actionHandler = foldHandlers(
    new UserHandler(userSelector),
    new CarHandler(carSelector)
  )
}

class CarHandler[M](modelRW: ModelRW[M, Seq[Car]]) extends ActionHandler(modelRW){
  override def handle = {
    case FetchCars => {
      val cars = Seq(Car("Rari"), Car("Lambo"))
      if (modelRW.value != cars) updated(cars) else noChange
    }
    
    case _ => {
      println("THE MODEL IS HERE", modelRW.value.toString)
      noChange
    }
  }
}
class UserHandler[M](modelRW: ModelRW[M, Seq[User]]) extends ActionHandler(modelRW){
  
//  implicit class conv[T](els: BindingSeq[T]) extends IndexedSeq[T]{
//    
//     override def length: Int = 0
//
//     override def size: Int = length
//
//     def count: Int = length
//
//     override def apply(idx: Int): T = els.head
//    
//    def convertToSeq() = {
//      var seq: Seq[T] = Seq.empty
//    
//    @dom
//    def extract = {
//      els.all.bind.foreach(x => seq = seq :+ x)
//    }
//    
//    @dom def exec = {
//      extract.bind
//    }
//    
//    exec
//    
//    seq.foreach(println)
//    
//    println("HRLLO")
//    
//    seq
//  }
//  }
    
    
  
  override def handle = {
    case Rename(id, name) => {
      val toRename = value.find(_.id == id).get
      //log.warn("old", toRename.name)
      val renamed = User(name, toRename.id)
      log.warn("STATE:", value.toString)
      //log.warn("new", renamed.name)
      //(value.filter(_.id != id) :+ renamed).foreach(x => log.warn("name: ", x.name))
      updated(value.map(x => if(x.id != id) x else renamed))
    }
    
    case ChangeId(oldId, newId) => {
      updated(value.map(x => x.id == oldId match {
        case true => User(value.find(_.id == oldId).get.name, newId)
        case _ => x
        }))
    }
    
    case FetchUsers => {
      //import ApiActions.fetchUsers
      
      //fetchUsers.map(users => updated(users))
      val users = Seq(User("Paul", 1), User("Tom", 2), User("Sam", 3))
      
      if (modelRW.value != users) updated(users) else noChange
    }
  }
}


object ApiActions {
  def fetchUsers = {
    val future = Ajax.get(
      url = "http://localhost:9000/api/users", 
      data = null, 
      timeout = 9000, 
      headers = Map.empty, 
      withCredentials = false, 
      responseType = "text")
      
//      import org.scalajs.dom.console
      import upickle.default._//readJs
      import scala.concurrent.ExecutionContext.Implicits.global
      
      future.map { xhr => 
          val users = readJs[Seq[User]](read(xhr.responseText))
          users
      }
  }
}