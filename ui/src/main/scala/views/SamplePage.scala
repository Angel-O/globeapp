package views

import components.Components.{Modal, Tab, Button}
import components.core.Implicits._
import router.RoutingView
import com.thoughtworks.binding.dom
import navigation.Navigators._

object SamplePage {
  def view() = new RoutingView() {

    @dom override def element = {

      val labelWithImage = <a>
                             <span class="icon is-small"><i class="fa fa-image"></i></span>
                             <span>Pictures</span>
                           </a>;

      val tab1 = <div>
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
				</div>;

      val tab2 =
        <div><Button label="Edit users" onClick={navigateToUserEdit _}/></div>;

      val tab3 = <div></div>;

      val tab4 = <div></div>;

      <TabSwitch 
		tabLabels={ Seq("Music", labelWithImage, "Words", "Images") }
		isRight={ false } 
		isCentered={ false } 
		isLarge={ true } 
		isFullWidth={ true } 
		tabContents={ Seq(tab1, tab2, tab3, tab4, "Is", "Not", "Used") }/>.build.bind
    }
  }
}
