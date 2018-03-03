package views.register

import router.RoutingView
import com.thoughtworks.binding.dom
import components.Components.Implicits.{CustomTags2, _}
import hoc.form._
import appstate.Connect
import appstate.VerifyUsernameAlreadyTaken
import appstate.Register
import navigation.URIs._
import navigation.Navigators._

object RegistrationPage {

  // navigating to home page after regitration to avoid "thread starvation" with login page...
  def view() =
    new RoutingView() with Connect {

      @dom override def element = {
        <div>
        <SimpleModal 
          openAtLaunch={true} 
          onSmartClose={navigateToHome _} content=
          {<div>
            <RegistrationForm 
              onSubmit={handleSubmit _}  
              verifyUsernameAlreadyTaken={verifyUsernameAlreadyTaken _}/>
          </div>}/>
      </div>
      }

      def verifyUsernameAlreadyTaken(username: String) = {
        dispatch(VerifyUsernameAlreadyTaken(username))
      }

      def handleSubmit(name: String,
                       username: String,
                       email: String,
                       password: String,
                       gender: String) = {
        dispatch(Register(name, username, email, password, gender))
      }
    }
}
