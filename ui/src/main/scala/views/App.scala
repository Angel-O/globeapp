package views

import org.scalajs.dom.document
import org.scalajs.dom.Node

import com.thoughtworks.binding._
import com.thoughtworks.binding.Binding._

import URIs._
import components.Components.Implicits._
import router.BrowserHistory
import router.RoutingView

import Tags.customTags
import org.scalajs.dom.raw.HTMLElement

object App {
  
  def main(args: Array[String]): Unit = {
    
    @dom def render = {
      
      // mapping view components (the actual pages to display)
      val routeMapping = mapViewsToURIs()  
      
      // creating wrappers around pages to provide routing capabilities
      val routes = createRouteComponents(routeMapping)
       
      // build the router (this could just be wrapped into a div, 
      // to handle building and binding automatically rather than 
      // calling them manually)
      <BrowserRouter routes={ routes.bind } />.build.bind
		}
    
    // mount the App
    dom.render(document.body, render.asInstanceOf[Binding[Node]])   
  }
  
  // TODO move this logic to a Factory class
  private def mapViewsToURIs(): List[(String, RoutingView)] = {
    
    import Navigators._
    val homePage = new RoutingView() { 
        @dom override def element =
          <div>
						<Tile 
							isAncestor={true}
							children={Seq(
							    <Tile 
										isVertical={true} 
										size={8}
										children={Seq(
										    <Tile children={Seq(
										        <Tile 
															isParent={true}
															isVertical={true} 
															children={Seq(
													      <Tile content={
													        <div>
																		<p class="title">Vertical...</p>
              											<p class="subtitle">Top tile</p>
																	</div>}
													    	/>,
													      <Tile content={
													        <div>
																		<p class="title">...tiles</p>
              											<p class="subtitle">Bottom tile</p>
																	</div>}
													    	/>)}
														/>,
														<Tile 
															isParent={true} 
															children={Seq(
										            <Tile 
																	content={
										                <div>
																			<p class="title">Middle tile</p>
              												<p class="subtitle">With an image</p>
              												<figure class="image is-4by3">
                												<img src="https://bulma.io/images/placeholders/640x480.png"/>
              												</figure>
																		</div>}
										        		/>)}
										    		/>
														)}
										    />,
										    <Tile isParent={true} 
														children={Seq(
										        <Tile content={
										          <div>
										      			<p class="title">Wide tile</p>
            										<p class="subtitle">Aligned with the right tile</p>
            										<div class="content">
              										<!-- Content -->
            										</div>
															</div>}
										        />)}
										    />
										    )}
									/>, 
							    <Tile 
										isParent={true}
							    	children={Seq(
							    	    <Tile
													content={
													  <div class="content">
            									<p class="title">Tall tile</p>
            									<p class="subtitle">With even more content</p>
            									<div class="content">
																<Button 
																		label={ "click me" } 
																		onClick={() => navigateToForm(history)}/>
              									<!-- Content -->
            									</div>
          									</div>}
												/>)}
							    />)}
						/>
					</div>
    } 
     
    //val form = customTags.RegistrationForm()
    
    val registerPage = new RoutingView() {
        @dom override def element = 
          <ModalCard 
						label={"Launch form"} 
						title={"Register now"} 
						content={<div><RegistrationForm/></div>}
						onSave={() => navigateToHome(history)}/>.build.bind }
    
//    val dashboardView = new RoutingView() {
//        @dom override def element = <Tile/>
//    }
      
    val routes = Map(
        HomePageURI -> homePage, 
        RegisterPageURI -> registerPage)
        
     routes.toList
  } 
  
  // TODO move this logic to a Factory class
  @dom private def createRouteComponents(routeMapping: List[(String, RoutingView)]) = {
    
    // yield uses a call back executed in another context where we cannot use the bind method
    // therfore we need to covert it to a binding sequence (under the hood the the component builder
    // will call the bind method...apparently)
    val routes = (for((uri, view) <- toBindingSeq(routeMapping)) 
                    yield <Route 
														path={uri} view={view}/>)
    
    routes
  }
}

object Navigators {  
  def navigateToForm(bh: BrowserHistory) = bh.navigateTo(RegisterPageURI)
  def navigateToHome(bh: BrowserHistory) = bh.navigateTo(HomePageURI) 
}

object URIs {
  val HomePageURI = "/"
  val RegisterPageURI = "/register"
}

