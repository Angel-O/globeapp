package components
import Components.Implicits._
import org.scalajs.dom.raw.{Event, HTMLElement}
import com.thoughtworks.binding.dom

case class CardBuilder() extends ComponentBuilder {
  def render = this
  var cardImage: ComponentBuilder = _
  var thumbnail: ComponentBuilder = _
  var title: String = _
  var subTitle: String = _
  var content: HTMLElement = _

  @dom def build =
    <div class={CARD}>
      { unwrapBuilder(cardImage) }
      <div class={CARD_CONTENT}>
        <div class={MEDIA}>
          <div class={MEDIA_LEFT}>
            { unwrapBuilder(thumbnail) }
          </div>
          <div class={MEDIA_CONTENT}>
            <p class="title is-4">{ title }</p>
            <p class="subtitle is-6">{ subTitle }</p>
          </div>
        </div>
        <div class={CONTENT}>
          { unwrapElement(content).bind }
        </div>
      </div>
    </div>
}
