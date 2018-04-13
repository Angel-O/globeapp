package views.register

import router.RoutingView
import com.thoughtworks.binding.dom
import components.core.Implicits._
import components.Components.Modal
import hoc.form.RegistrationForm
import appstate.{Connect, Register, VerifyUserAlreadyRegistered}
import appstate.AppCircuit._
import navigation._, URIs._, Navigators._
import apimodels.user.AppUser

object RegistrationPage {

  // navigating to home page after registration to avoid "thread starvation" with login page...
  def view() =
    new RoutingView() {

      @dom override def element = {
        <div>
        <PageModal 
          isPageModal={true}
          isOpen={true} 
          onSmartClose={navigateToHome _} content=
          {<div>
            <RegistrationForm 
              onSubmit={handleSubmit _}  
              verifyEmailAlreadyTaken={verifyEmailAlreadyTaken _}/>
          </div>}/>
      </div>
      }

      def verifyEmailAlreadyTaken(email: String) = {
        dispatch(VerifyUserAlreadyRegistered(email))
      }

      def handleSubmit(
        name:                   String,
        username:               String,
        email:                  String,
        password:               String,
        gender:                 String,
        whereDidYouHearAboutUs: String,
        additionalInfo:         String,
        subscribed:             Boolean) = {
        dispatch(Register(name, username, email, password, gender, AppUser, whereDidYouHearAboutUs,
          additionalInfo,
          subscribed)) //TODO create page to register devuser
      }
    }
}
