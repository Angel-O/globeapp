package views.register

import router.RoutingView
import com.thoughtworks.binding.dom
import components.core.Implicits._
import components.Components.Modal
import hoc.form.RegistrationForm
import appstate.{Connect, Register, VerifyUsernameAlreadyTaken}
import appstate.AppCircuit._
import navigation._, URIs._, Navigators._

object RegistrationPage {

  // navigating to home page after registration to avoid "thread starvation" with login page...
  def view() =
    new RoutingView() {

      @dom override def element = {
        <div>
        <SimpleModal 
          isPageModal={true} 
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
