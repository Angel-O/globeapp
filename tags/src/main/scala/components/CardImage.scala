package components

import components.core.Implicits._
import org.scalajs.dom.raw.{ Event, HTMLElement }
import com.thoughtworks.binding.dom
import components.core.ComponentBuilder


case class CardImageBuilder() extends ComponentBuilder { 
    def render = this
    var url: String = _    
    var alt: String = _   
    
    //TODO make size variable   
    @dom def build = <figure class="image is-48x48">
      								<img src={url} alt={alt}/>
    								 </figure>
  }
