package views.poll

import components.core.Implicits._
import components.core.ComponentBuilder
import components.core.Color
import components.core.Helpers._
import components.Components.{Layout, Modal, Misc}
import com.thoughtworks.binding.{dom, Binding}, Binding.Var, Binding.Vars
import apimodels.poll.Poll
import utils.nameOf._
import java.time.format.DateTimeFormatter
import org.scalajs.dom.raw.Event

case class PollDetailDialogBuilder() extends ComponentBuilder with Color {

  def render = this
  
  var targetPoll: Option[Poll] = None
  var dialogIsOpen: Boolean = _
  var handleClose: () => Unit = _
  var appName: String = _
  var appId: String = _
  var castVote: (String, Int) => Unit = _
  var canVote: Boolean = _

  private val dateFormatter = DateTimeFormatter.ofPattern("dd LLLL yyyy");

  @dom
  def build = {

    val dialog = toBindingSeq(targetPoll).map(poll => {

      val totalVotes = poll.options.map(_.votedBy.size).sum

      val results = toBindingSeq(poll.options)
        .map(option => 
          <li>
            <Box sizes={Seq(`4/5`, `1/5`)} contents={Seq(
              <span>{option.content}</span>,
              <div>
                <Box sizes={Seq(`2/3`, `1/3`)} contents={Seq(
                  <span style={"font-size: 0.8em"}>
                    {s"${getVotesPercentage(option.votedBy.size, totalVotes)} %"}
                  </span>, { unwrapElement(
                  <span style={"cursor: pointer"} onclick={(_: Event) => castVote(poll._id.get, option.id)}>
                    <Icon id="thumbs-up"/>
                  </span>, canVote).bind }
                )}/>
              </div> 
            )}/> 
          </li>)
        .all
        .bind

      val popupHeader = s"Poll: ${poll.title} - (closing date: ${poll.closingDate.format(dateFormatter)})"

      val dialogContent =
        <div>
            <Message header={popupHeader} isPrimary={true} 
            isMedium={true} style={"padding: 1em"} content={ 
              <div>
                <h1> App: <a href={s"#/globeapp/catalog/$appId"}>{ appName }</a> </h1> <br/>
                <h1> Content: { poll.content } </h1> <br/>
                <ul> { results } </ul>
              </div>}/>
        </div>

      val modal =
        <div>
            <SimpleModal
              onClose={handleClose} 
              content={dialogContent}
              isOpen={dialogIsOpen}/>
        </div>

      modal
    })

    <div>{ dialog.all.bind }</div>
  }

  def getVotesPercentage(votes: Int, totalVotes: Int) = votes match {
    case 0 => 0.0
    case _ => votes / totalVotes.toDouble * 100
  } 
}

//import com.thoughtworks.binding.Binding.BindingSeq
//import com.thoughtworks.binding.Binding.Constants
//import org.scalajs.dom.raw.{Event, HTMLElement}
//def render = {

    //ooo.asInstanceOf[Var[Unit]].value = ooo.asInstanceOf[Var[Unit]].value
    //Var(test).value = null
    //test.asInstanceOf[Seq[Any]].head //= test.asInstanceOf[Var[Any]].value
//    this
//  }

//  lazy val ooo:BindingSeq[Var[Unit]] = toBindingSeq(Seq(Var({
//    val dummies = toBindingSeq(Seq(build)).map(_.bind.querySelectorAll(".dummy").map(_.asInstanceOf[HTMLElement]))
//    println("dummies", dummies)
//    dummies.foreach(x => x.head.parentElement.removeChild(x.head))
//  })))
  
//  lazy val test = {
//    @dom val dummies = 
//      Var(
//        toBindingSeq(Seq(build))
//        .map(e => {println("e",e);  e.bind.bind.querySelectorAll(".dummy").map(_.asInstanceOf[HTMLElement])})
//        .all.bind.map(x => {println("INSIDE", x.head.innerHTML); x.head.parentElement.removeChild(x.head)}))
//    println("dummies", dummies)
//    //dummies.foreach(x => {println("INSIDE", x.head.innerHTML); x.head.parentElement.removeChild(x.head)})
//    println("HELLO")
//    val r: Var[Any] = Var(dummies)
//    
//    val b: Binding[Any] = r
//    
//    //toBindingSeq(Seq(b)).map(_.bind).all ====> crrashes compiler
//  }