package example

import org.scalajs.dom.document
import com.thoughtworks.binding._, Binding._
import org.scalajs.dom.raw.Node
import org.scalajs.dom.raw.HTMLElement
import org.scalajs.dom.raw.NodeListOf
import org.scalajs.dom.raw.Event

import componentsTrial.Button
import components.button.{ ButtonRaw => Btn }
import components.{InputRaw => Ipt}
import hoc._
import components.Components.Implicits.{CustomTags2, _}
import components.dropdown.MenuItemBuilder
import org.scalajs.dom.raw.HTMLInputElement
import org.scalajs.dom.raw.MutationObserver
import org.scalajs.dom.raw.MutationRecord

import scalajs.js
import org.scalajs.dom.raw.MutationObserverInit
import org.scalajs.dom.raw.HTMLParagraphElement
import hoc.form._

import org.scalajs.dom.ext.Ajax
import scala.concurrent.Future

//import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}
import org.scalajs.dom.ext.Ajax.InputData

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

//import scala.scalajs.concurrent.JSExecutionContext.queue

//import org.scalajs.dom.raw.{ Event, HTMLInputElement, HTMLButtonElement, HTMLDivElement, HTMLElement, HTMLAnchorElement }
//import org.scalajs.dom.html.Div
//import scala.scalajs.js
//import scala.xml.{Elem, MetaData, NamespaceBinding, Node => XmlNode, UnprefixedAttribute, NodeSeq}
//import macros.RegisterTag._

object Hello  {
     
  def main2(args: Array[String]): Unit = {
  
    val styler = Var("color:blue; font-style:italic")
    val label1 = Var("click me mate")
    val label2 = Var("click me mate")
      
    val handleClick = (clicked: Var[Boolean], label: Var[String]) => {
      val toggleLabel = () => { 
        clicked.value = !clicked.value
        label.value = if (clicked.value) "clicked" else "click me again"
      }
      toggleLabel()
    }
    
    val button1 = Button(label1, handleClick)
    val button2 = Button(label2, handleClick)
    
    val log = Var("Name here")
    val log2 = Var("Hello")
    val log3 = Var("ZZZZZ")
    val disabled = Var(log.value == "Name here")
    
    val btn = Btn("real btn", () => println("REAL"))
    //val input = Ipt(log.bind, (value: String) => log.value = value, placeholder = "Text goes here") 
    
    val handler = () => {
      println("Hello")
      styler.value = "color:red; font-style:italic"
      label1.value = "click me mate"
      label2.value = "click me mate"
      //button2.value = button1.value  //===> not working...
    }
    
    //val newButton = 
											
    
    val buttons = Vars(Button("Click me", handler), Button("What's up", handler)) 

    @dom def createTitle(text: F[String], styler: Var[String]) = <h2 style={styler.bind}>{text.bind}</h2>
    
    @dom def createTitle2(text: String, styler: Var[String]) = <h2 style={styler.bind}>{text}</h2>
    
    @dom def createInput(text: Var[String]) = {
      //Note: using text.value rather than text.bind...
      Ipt(text.value, (value: String) => text.value = value, placeholder = "Text goes here").bind 
    }
    
    @dom def createInput2(text: String) = {
      Ipt(text, (value: String) => log.value = value, placeholder = "Text goes here").bind 
    }
    
    val input = Ipt("Type for magic v2", (value: String) => log.value = value, placeholder = "Text goes here", disabled = log.value == "ciao")
    
    @dom def createInput3(text: Var[String]) = {
      //Note: using text.bind rather than text.value...
      // input and text are bound in the same context...they will reload at the same time...
      Ipt(text.bind, (value: String) => text.value = value, placeholder = "Text goes here").bind 
    }
    
    @dom def renderTable(log: String) = {

      //TODO make cells accept html elements...
      val table = 
      <Table 
				header={ <TableHeader cells={ Seq(log, "World", "!") }/> } 
				rows={Seq(
            <TableRow cells={ Seq(log, "2", "3") }/>,
            <TableRow cells={ Seq("4", "5", "6") }/>,
            <TableRow cells={ Seq("7", "8", "9") }/>,
            <TableRow cells={ Seq("10", "11", "12") }/>)} 
				footer={ <TableFooter cells={ Seq(log, "World", "!") }/> } 
				isBordered={ true } 
				isStriped={ true } 
				isHoverable={ true } 
				isFullWidth={ true }/>;
			
			table
    }
    
    @dom def renderDropdown(log: String) = {
      
      //val items = List(log, "World", "This", "Is", "A", "MenuItem")
      //val menuItems = items.map(x => <MenuItem itemText={x}/>)
      <Dropdown 
				label="Drop me man!" 
				menuItems={Seq(log, "World", "This", "Is", "A", "MenuItem")}
			/>
    }
    
    @dom def renderLog(text: Var[String]) = text.bind
    
    val future = FutureBinding(Ajax.get("http://jsonplaceholder.typicode.com/posts/1", null, 9000, Map.empty, false, "application/json"))
    
    val futureInt = FutureBinding(Future(5))
    
    @dom def renderFuture = {
      val arrived = future.bind match {
        case Some(Success(xhr)) => {
          val json=js.JSON.parse(xhr.responseText)
          val title=json.title.toString
          val body=json.body.toString
          
          s"$title, $body"
        }
        case Some(Failure(error)) => s"$error"
        case _ => "nothing"
      }
      
      arrived
    }
   
    @dom def render = {    
            
      //renderLog apparently won't return a string (or not yet) it returns a F (Monadic factory, that will
      // probably return a string at some point...??? not sure)
      val logs = Vars(renderLog(log), renderLog(Var("Binding.scala and Bulma, with custom comps 2!")))
      
      // leave commented out: binding in this context would change the behaviour, even if logs2 in not used
      //val logs2 = Vars(log.bind, log2.bind)
      val bef = List("Hello", "World", "This", "Is", "A", "MenuItem")
      
      
      //val rows = items.map(x => <TableRow cells={List(x)}/>)
     
      //TODO make this a reusable component
//      val labelWithImage2 = <a class="button">
//														<span class="icon is-small"><i class="fa fa-image"></i></span>
//        										<span>Pictures</span>
//													 </a>.asInstanceOf[HTMLElement];
			val labelWithImage2 = <SimpleButton 
															label="Pictures" 
															icon={<a class="fa fa-image"/>}
      												onClick={() => println("icon")}/>
      val labelWithImage3 = <SimpleButton 
															label="Pictures" 
															icon={<a class="fa fa-image"/>}
      												onClick={() => println("icon")}/>
      
      
    
      val tab4 = <div>
										<h1>{renderFuture.bind}</h1>
										<!-- <RegistrationForm/> -->							
									</div>
                   
                   
      val tab2 = <div>
					
      		<div class="notification columns is-mobile">
          { for { lg <- logs } yield <div class="column is-half"> { createTitle2(lg.bind, styler).bind } </div> } 
  				</div>
      		<div style="display: flex; justify-content: space-between">
      	    { buttons.map(x => x.bind) }
      	    { button1.bind }
      	    { button2.bind }
      	    { btn.bind }
      	    { labelWithImage2 }
      	    <div>
							<MyComponent 
								foo="Hello, World" 
								inner={<p>
												<h1>UUUUUU</h1>
												<section>bla bla</section>
												{val el = <a href={"#"}>LINK</a>
												el}
												{<div>{input.bind}</div>}
												<MyComponent foo="bla bla" inner={
													<div>
														<h2>DONE</h2>
														<CardImage url="https://bulma.io/images/placeholders/96x96.png" alt="kk"/>
													</div>}
												/>
											</p>
												}
									/>
						</div>
					</div>
					{renderDropdown(log.bind).bind}
					<Dropdown 
						label="Drop me man!" 
						menuItems={Seq(log.bind, "World", "This", "Is", "A", "MenuItem")} 
					/> <!-- try and pass dependencies to subscribe to log changes... -->
					<ModalCard 
						label={"modal Z"} 
						title={"A modal"} 
						content={<div>Modal content</div>} 
						isPrimary={true}
						isSmall={true}
						isMedium={false}
						isLarge={true}
						onSave={() => println("MODAL Z saved")}
						smartClose={false}/>
					<SimpleModal
						label={"Simple modal"}
						content={<div><Card 
						cardImage={<CardImage url="https://bulma.io/images/placeholders/1280x960.png" alt="hey"/>}
						thumbnail={<CardImage url="https://bulma.io/images/placeholders/96x96.png" alt="kk"/>}
						title="Hello"
						subTitle="World 2"
						content={<div>
												Lorem ipsum dolor sit amet, consectetur adipiscing elit. Phasellus nec iaculis mauris. 
						         		<a>@bulmaio</a>
      							 		<a href="#">#css</a> <a href="#">#responsive</a>
      							 		<br/>
      							 		<time data:datetime="2016-1-1">11:09 PM - 1 Jan 2016</time>
												<CardImage url="https://bulma.io/images/placeholders/96x96.png" alt="kk"/>
												<Input 
													label="type ciao"
													placeholder="Reusable v2"  		 
													onChange={(value: String) => log.value = value}
													isDisabled={() => Binding {log.bind == "ciao"}}/>
										 </div>}
					/></div>}/>
					<Card 
						cardImage={<CardImage url="https://bulma.io/images/placeholders/1280x960.png" alt="hey"/>}
						thumbnail={<CardImage url="https://bulma.io/images/placeholders/96x96.png" alt="kk"/>}
						title="Hello"
						subTitle="World 2"
						content={<div>
												Lorem ipsum dolor sit amet, consectetur adipiscing elit. Phasellus nec iaculis mauris. 
						         		<a>@bulmaio</a>
      							 		<a href="#">#css</a> <a href="#">#responsive</a>
      							 		<br/>
      							 		<time data:datetime="2016-1-1">11:09 PM - 1 Jan 2016</time>
												<CardImage url="https://bulma.io/images/placeholders/96x96.png" alt="kk"/>
												<Input 
													label="type ciao"
													placeholder="Reusable v2"  		 
													onChange={(value: String) => val kk = List() ++ value; println(kk); ()}
													isDisabled={() => Binding {log.bind == "ciao"}}/>
										 </div>}
					/>
					<!-- Note how they behave differently: Input does not reset the value of the input box
								while InputRaw does when it becomes disabled...Investigate -->
					<InputRaw 
						placeholder="Reusable" 
						label="type bull"
						onChange={(value: String) => log.value = value}
      			isDisabled={() => Binding {log.bind == "bull" && log2.bind == "Hello"}}/>
					<Input 
						label="type ciao"
						placeholder="Reusable v2"  		 
						onChange={(value: String) => log.value = value}
						isDisabled={() => Binding {log.bind == "ciao"}}/>
					<div>
						{ createInput(log).bind }
						{ 
						  //this is able to change the title without losing-focus on re-rendering because
						  // it changes the value member of a component (log) bound outside this binding block (render) 
						  // basically the two binding blocks (defined by @dom) are re-rendered 
						  // separately...(render vs renderLog)			  
						  //input.bind
						  val hi = "4"
						  hi
						  //createInput2(log.value).bind
						 }
						{ createInput3(log2).bind }
						{
						  val isDisabled = log.bind == "ciao"
						  <Button 
							label="NEW BUTTON" 
							onClick={() => println(log2.value)}
    					isDisabled={isDisabled} 
							isPrimary={true}/>.listen}
						<!--<Table
							rows={rows}/>-->
						
						{renderTable(log.bind).bind}
					</div>
			</div>;
			
			//TODO make this a reusable component
      val labelWithImage = <a>
														<span class="icon is-small"><i class="fa fa-image"></i></span>
        										<span>Pictures</span>
													 </a>;

      val rightItems = Constants(<div class="navbar-item has-dropdown">
                         <a class="navbar-link">
                           Docs
                         </a>
                         <div class="navbar-dropdown">
                           <a class="navbar-item">
                             Overview
                           </a>
                         </div>
                       </div>,
                       <div class="navbar-item">
                         <div class="field is-grouped">
                           <p class="control">
                             <a class="button">
                               <span class="icon">
                                 <i class="fa fa-twitter" data:aria-hidden="true"></i>
                               </span>
                               <span>Tweet</span>
                             </a>
                           </p>
                           <p class="control">
                             <a class="button is-primary">
                               <span class="icon">
                                 <i class="fa fa-download" data:aria-hidden="true"></i>
                               </span>
                               <span>Download</span>
                             </a>
                           </p>
                         </div>
                       </div>);
      
      val clickableItem = <div><h4>Element here mate</h4><p>What is this?</p></div>.asInstanceOf[HTMLElement]
      clickableItem.addEventListener("click", (_: Event) => println("Yup"))
      
			val main = 
        <div class="container is-fluid">
					<Navbar
						isFixedBottom={true} 
						isTransparent={true}
						logo={<NavbarLogo
										image={<img 
														src={"https://bulma.io/images/bulma-logo.png" } 
														alt={"Bulma: a modern CSS framework based on Flexbox"}
														width={112}
														height={28}/>}
										href={"#"}
									/>}
      			leftItems={Seq(<NavbarItem 
																	item={"Home 2"}
																	isHoverable={true}
																	dropdownItems={Seq(
																	    "Shoes", 
																	    "Hats", 
																	    <hr class="dropdown-divider"/>,
																	    "Books", 
																	    clickableItem,               
                                      <div class="field is-grouped">
                    										<p class="control">
                      										<a class="button">
                        										<span class="icon">
                          										<i class="fa fa-twitter" data:aria-hidden="true"></i>
                        										</span>
                        										<span>Tweet</span>
                      										</a>
                    										</p>
                    										<p class="control">
                      										<a class="button is-primary">
                        										<span class="icon">
                          										<i class="fa fa-download" data:aria-hidden="true"></i>
                        										</span>
                        										<span>Download</span>
                      										</a>
                    										</p>
                  										</div>)}/>,
																	<NavbarItem 
																	item={"House"}/>,
																	<NavbarItem 
																	item={<div>Element here</div>}
																	isExpanded={true}/>)}
					rightItems={
            Seq(<NavbarItem 
												item={
                          <a class="button">
                						<span class="icon">
                  						<i class="fa fa-twitter" data:aria-hidden="true"></i>
                						</span>
                						<span>Tweet</span>
              						</a>}
												/>,
												<NavbarItem 
													item={labelWithImage3}/>,
												<NavbarItem
													item={<Dropdown 
																label="Drop me from up here man!" 
																menuItems={Seq(log.bind, "World", "This", "Is", "A", "MenuItem")} />}
												/>)
          }/>
          <TabSwitch 
						tabLabels={ Seq("Music", labelWithImage, log, "Images") }
						isRight={ false } 
						isCentered={ false } 
						isLarge={ true } 
						isFullWidth={ true } 
						tabContents={ Seq(
						    <div>
									<p>Hello world</p> 
									<ModalCard 
										label={"modal 1"} 
										title={"A modal"} 
										content={<div>Modal content</div>} 
										onSave={() => println("MODAL 1 saved")}/>
									<ModalCard 
										label={"Launch form"} 
										title={"This is a modal"} 
										content={<div>No form</div>}
										onSave={() => println("MODAL 2 saved")}/>
								</div>, 
								tab2, log, tab4, "Is", "Not", "Used") }/>
        </div>; 
            <!-- 
								needs to be wrapped into a div or an instance of Node, 
								NOTE semi-colon!!!! it might not work without (it seems like it cannot distinguis where
								the div ends)-->
			
			main
    }	
    
//    var observer = new MutationObserver(
//        (mutations: js.Array[MutationRecord], obs: MutationObserver) => 
//          mutations.foreach(x => {
//            val item = x.addedNodes.item(0)
//            if(item != null && item.isInstanceOf[HTMLParagraphElement]){
//              println(item)
//              item.asInstanceOf[HTMLParagraphElement].innerHTML = "Mutation mate"
//            }
//            
//            }))
//    
//    val contentObserverParams = new js.Object{
//        val subtree = true
//        val attributes = true
//        val childList =true
//        val characterData = true
//        val characterDataOldValue =true
//      }.asInstanceOf[MutationObserverInit]
//    
//    observer.observe(document.body, contentObserverParams)
    
    dom.render(document.body, render.asInstanceOf[Binding[Node]])   
    
    // attach global events here...after dom has been rendered...
    val drops = getAll("div").asInstanceOf[NodeListOf[HTMLElement]]
    
    //TODO remove based on id...don't rely on parent...what if a dummy is a parent of a dummy??
    val dummies = document.querySelectorAll(".dummy").map(_.asInstanceOf[HTMLElement])
    dummies.foreach(x => x.parentElement.removeChild(x))
    val ff = drops.filter(x => x.classList != null)
    println("IS IT", drops.isInstanceOf[Seq[_]])
    println("DROPS COUNT:", drops.length)
    
//    var observer = new MutationObserver((mutations: js.Array[MutationRecord], obs: MutationObserver) => mutations.foreach(println))
//    
//    val contentObserverParams = new js.Object{
//        val subtree = true
//        val attributes = true
//        val childList =true
//        val characterData = true
//        val characterDataOldValue =true
//      }.asInstanceOf[MutationObserverInit]
//    
//    observer.observe(document.body, contentObserverParams)
    
//    document.getElementById("hello").asInstanceOf[HTMLInputElement].focus
//    document.getElementById("hello").asInstanceOf[HTMLInputElement].select

  }
}
