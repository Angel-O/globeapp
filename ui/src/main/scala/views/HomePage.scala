package views

import router.RoutingView
import org.scalajs.dom.document

import com.thoughtworks.binding._
import com.thoughtworks.binding.Binding._

import URIs._
import components.Components.Implicits._
import router.RoutingView

import org.scalajs.dom.raw.HTMLElement
import com.thoughtworks.binding.dom
import org.scalajs.dom.raw.HTMLElement

object HomePage {
  
  import Navigators._
  val homePage = new RoutingView() { 
    @dom override def element = html(() => navigateToForm(history)).bind
  }
  
  //TODO split into subtiles and create HomePage subpackage 
  @dom def html(navigateToForm: () => Unit) = {
          <div>
						<Tile isAncestor={true} 
							children={Seq(
							  <Tile isVertical={true} width={8}
							  	children={Seq(
							  	    <Tile 
												children={Seq(
												    <Tile isParent={true} isVertical={true} 
															children={Seq(
													      <Tile isPrimary={true}
																	content={
													          <div>
																			<p class="title">Vertical...</p>
              												<p class="subtitle">Top tile</p>
																		</div>}
													    	/>,
													      <Tile isWarning={true}
																	content={
													          <div>
																			<p class="title">...tiles</p>
              												<p class="subtitle">Bottom tile</p>
																		</div>}
													    	/>)}
														/>,
														<Tile isParent={true} 
															children={Seq(
										            <Tile isInfo={true}
																	content={
										                <div>
																			<p class="title">Middle tile</p>
              												<p class="subtitle">With an image</p>
              												<figure class="image is-4by3">
                												<img src="https://bulma.io/images/placeholders/640x480.png"/>
              												</figure>
																		</div>}
										        		/>)}
										    		/>)}
										    />,
										    <Tile isParent={true} 
														children={Seq(
										        <Tile isDanger={true}
										        	content={
										            <div>
										      				<p class="title">Wide tile</p>
            											<p class="subtitle">Aligned with the right tile</p>
            											<div class="content">
              											<!-- Content -->
            											</div>
																</div>}
										        />)}
										    />)}
								/>, 
							  <Tile isParent={true}
							    	children={Seq(
							    	    <Tile isSuccess={true} onClick={navigateToForm}
													content={
													  <div class="content">
            									<p class="title">Tall tile</p>
            									<p class="subtitle">With even more content</p>
            									<div class="content">
																<Button 
																		label={ "click me or the whole tile" } 
																		onClick={navigateToForm}/>
              									<!-- Content -->
            									</div>
          									</div>}
												/>)}
							   />)}
						/>
					</div>.asInstanceOf[HTMLElement] 
  }
}