package appstate

import diode.Circuit
import diode.ActionHandler
import diode.ModelRW
import apimodels.user.User
import diode.Dispatcher
import diode.ModelR
import diode.data.PotState._
import diode.data.Pot
import diode.Effect
import diode.Action
import utils.log
import scalajs.js
//import upickle.default.{ReadWriter => RW, macroRW}
  
import components.core.ComponentBuilder

// Represents the portion of the state that will be serialized
// in the location storage to be retrieved after a browser refresh
case class PersistentState(user: User)
case object PersistentState{
  //def apply(username: String, userId: String) = new PersistentState(username, userId)
  //def apply() = new PersistentState("", "") //TODO use option after finding out how to persist global state
  //implicit def rw: RW[PersistentState] = macroRW
}

// Global state tree
case class AppModel(users: Users, cars: Cars, auth: Auth, mobileApps: MobileApps, polls: Polls, reviews: Reviews, suggestions: Suggestions)

object AppCircuit extends Circuit[AppModel] with ModelLens[AppModel] with GlobalSelector[AppModel] with HelpConnect[AppModel] {

  def initialModel = AppModel(Users(), Cars(), Auth(), MobileApps(), Polls(), Reviews(), Suggestions())

  def currentModel = zoom(identity).value

  val userSelector = zoomTo(x => x.users.users)
  val carSelector = zoomTo(x => x.cars.cars)
  val authSelector = zoomTo(x => x.auth.params)
  val mobileAppSelector = zoomTo(x => x.mobileApps.apps)
  val pollSelector = zoomTo(x => x.polls.polls)
  val reviewSelector = zoomTo(x => x.reviews.reviews)
  val suggestionSelector = zoomTo(x => x.suggestions.state)
  
  implicit val globalSelector: ModelRW[AppModel, AppModel] = zoomRW[AppModel](identity)((model, _) => identity(model))
  
  // Using foldHandlers rather than composeHandlers to
  // allow all handlers to process the actions without stopping
  // soon as the the action has been handled
  override val actionHandler = foldHandlers(
    new LoggingHandler(globalSelector),
    new UserHandler(userSelector),
    new CarHandler(carSelector),
    new AuthHandler(authSelector),
    new MobileAppsHandler(mobileAppSelector),
    new PollHandler(pollSelector),
    new ReviewHandler(reviewSelector),
    new SuggestionHandler(suggestionSelector)
  )
  
  val circuit = this
}

trait GlobalSelector[M]{
  val globalSelector: ModelRW[M, M]
}

trait HelpConnect[M <: AnyRef] {
  implicit val circuit: Circuit[M] with GlobalSelector[M]
  private var unsubscribe: Option[() => Unit] = None
  private var multiUnsubscribe: Seq[() => Unit] = Seq.empty 
  
  def multiConnect(connector: => Unit)(cursors: ModelR[M, _]*) = {
    val defaultCursors = if (cursors.isEmpty) Seq(circuit.globalSelector.root.asInstanceOf[ModelR[M, M]]) else cursors
    // similar approach used for individual connects (unsubscribe first
    // then re-subscribe), applied to all cursors
    multiUnsubscribe.foreach(_.apply())
    multiUnsubscribe = Seq.empty
    defaultCursors.foreach(cursor => multiUnsubscribe = multiUnsubscribe :+ circuit.subscribe(cursor)(_ => connector))
  }
    
  // connect can be called from a routing view, each time the view is rendered: it's inefficient
  // therefore it is a good thing to unsubscribe first, then resubscribe
  def connect[T](connector: => Unit)(implicit cursor: ModelR[M, T] = circuit.globalSelector.root.asInstanceOf[ModelR[M, M]]) = {   
    //unsubscribe.map(handler => handler()) //TODO while the above is true(???)...it prevents 
    // updates from being propagated...commented out for now (test loggedIn var in App.scala)
    val h = circuit.subscribe(cursor)(_ => connector)
    //println("HANDLER", h)
    unsubscribe = Option(h)
  }
    
    
  def dispatch(action: Action) = circuit.apply(action)

  // import scala.concurrent.Future
  // import scala.concurrent.ExecutionContext.Implicits.global
  // def dispatchAll(actions: Seq[Action]): Future[Unit] = actions match {
  //   case h::t => Future { dispatchAll(Seq(h)) }.map( _ => dispatchAll(t) )
  //   case h::Nil => Future { dispatchAll(Seq(h)) }
  //   case _ => Future.unit
  // }
}

class LoggingHandler[M](modelRW: ModelRW[M, AppModel])
    extends ActionHandler(modelRW)
    with GenericConnect[AppModel, AppModel] {
  import utils.log
  import diode.NoAction
  override def handle = {
    case a: Action => {
      if (a != NoAction) {
        log.warn("ACTION", a.asInstanceOf[js.Any])
        log.warn("STATE-BEFORE", value.asInstanceOf[js.Any])
      }
      noChange
    }
  }

  val circuit = AppCircuit
  def connectWith() = log.warn("STATE-AFTER: ", circuit.currentModel.asInstanceOf[js.Any])
  val cursor = modelRW.root.asInstanceOf[ModelR[AppModel, AppModel]]

  connect()
}


// TODO move this to separate project...ala Redux
trait ModelLens[M <: AnyRef] {
  def initialModel: M
  def currentModel: M
}
abstract class Connector[M <: AnyRef](circuit: Circuit[M] with ModelLens[M])
    extends ComponentBuilder
    with ModelLens[M] {

  def dispatch(action: Action) = circuit.apply(action)

  def initialModel = circuit.initialModel
  def currentModel = circuit.currentModel

  def connect[M <: AnyRef, T]()(cursor: ModelR[M, T] = circuit.zoom(identity),
                                update: => Unit = Unit) {

    val ac: Circuit[M] = circuit.asInstanceOf[Circuit[M]]
    
    ac.subscribe(cursor)(_ => update)
  }
}

abstract class ConnectorBuilder extends Connector[AppModel](AppCircuit)

//Use generic connect rather than Connect since generic connect
//is decoupled from AppCircuit and use only ConnectorBuilder or Connector
trait Connect {
  def dispatch(action: Action) = AppCircuit.apply(action)
  def initialModel = AppCircuit.initialModel
  def value = AppCircuit.currentModel

  //TODO investigate: equal sign is missing before method body and
  // double set of params is weird...
  def connect[M <: AnyRef, T]()(
      cursor: ModelR[M, T] = AppCircuit.zoom(identity),
      update: => Unit = Unit) {

    val ac: Circuit[M] = AppCircuit.asInstanceOf[Circuit[M]]
    
    ac.subscribe(cursor)(_ => update)
  }
}

trait GenericConnect[M <: AnyRef, T] extends ConnectWith {
  def dispatch(action: Action) = circuit.apply(action)
  
  val cursor: ModelR[M, T]
  val circuit: Circuit[M] with ModelLens[M]

  def connectWith(): Unit

  protected def model = cursor.value
  protected def initialModel = circuit.initialModel
  
  protected def connect() = circuit.subscribe(cursor)(_ => connectWith())
}

// Note: defining this method separately because the compiler complains about empty names
trait ConnectWith{
  def connectWith(): Unit
}

trait SilentConnect[M <: AnyRef,T] extends GenericConnect[M,T]{
  def connectWith() = Unit
  val cursor = null
  val circuit = null //TODO needs to be able to dispatch..this can't be null
  override def connect() = () => Unit
}




//////// TODO improve this: the goal is to allow to connect to multiple selectors 

abstract class ReadWriteConnectBase[M <: AnyRef, T] {
  val cursor: ModelR[M, T]
  val circuit: Circuit[M] with ModelLens[M]
}

trait ReadConnect[M <: AnyRef, T] extends ReadWriteConnectBase[M, T] {
  protected def model = cursor.value
  protected def initialModel = circuit.initialModel
}

trait WriteConnect[M <: AnyRef, T] extends ReadWriteConnectBase[M, T] {

  def connect(connector: => Unit) = circuit.subscribe(cursor)(_ => connector)
}

trait RWConnect[M <: AnyRef, T] extends ReadConnect[M, T] with WriteConnect[M, T]

trait AppModelSelector[T] extends ReadConnect[AppModel, T]{
  import apimodels.common.localDateOrdering
  implicit val ordering = localDateOrdering
}

