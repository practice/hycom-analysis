package com.miraeclimate.climateconvergence.hycom

import akka.actor.Actor
import akka.actor.Actor.Receive

/**
 * Created by shawn on 15. 1. 7..
 */
class HelloActor extends Actor {
  override def receive: Receive = {
    case "hello" => println("hello back at you")
    case _ => println("huh?")
  }
}
