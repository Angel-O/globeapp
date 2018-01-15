package components
import Components.Implicits._
import org.scalajs.dom.raw.{ Event, HTMLElement }
import com.thoughtworks.binding.dom


case class CardBuilder() extends ComponentBuilder {
  def render = this
  var cardImage: ComponentBuilder = _
  var thumbnail: ComponentBuilder = _
  var title: String = _
  var subTitle: String = _
  var content: HTMLElement = _
      
  @dom def build = <div class="card">
  								   {unwrapBuilder(cardImage)}
      							 <div class="card-content">
										 	<div class="media">
												<div class="media-left">
													{unwrapBuilder(thumbnail)}
												</div>
												<div class="media-content">
        									<p class="title is-4">{title}</p>
        									<p class="subtitle is-6">{subTitle}</p>
      									</div>
											</div>
											<div class="content">
      								  {unwrapElement(content).bind}
    									</div>
										 </div>
    							 </div>.asInstanceOf[HTMLElement]
    }