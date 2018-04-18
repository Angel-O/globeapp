package hoc.panel

import apimodels.mobile.MobileApp
import components.core.Implicits._
import components.core.ComponentBuilder
import components.core.Color
import components.core.Helpers._
import components.Components.{ Input, Layout }
import com.thoughtworks.binding.{dom, Binding}, Binding.Var

case class AppsPanelBuilder() extends ComponentBuilder with Color {
  def render = this
  
  var apps: Seq[MobileApp] = Seq.empty
  var header: String = _
  
  @dom def build = {
    <div>
		  <Message colorClass={COLOR_CLASS} header={header} content={
		    <div>
			  		<ul>{ toBindingSeq(apps).map(app => 
			  		  <li>
			  				<a href={s"#/globeapp/catalog/${app._id.get}"}>{ app.name }</a> 
			  					<span> ({ app.store }) </span> 
			  		  </li>) }
          </ul>
       </div>
      }/>
    </div>
  }
}