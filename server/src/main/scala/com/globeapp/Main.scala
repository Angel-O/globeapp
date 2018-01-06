package com.globeapp

import akka.actor.ActorSystem

class Main extends App{
  
  // create the actor system, implicit to amke it available to whever needs it
  implicit val system = ActorSystem("server")
}