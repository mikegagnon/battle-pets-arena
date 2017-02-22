package me.michaelgagnon.pets.web.actors

import akka.actor.ActorSystem
import akka.actor.Props
import java.util.UUID

import me.michaelgagnon.pets.web.controllers.ContestRequest

case class ContestWithId(contest: ContestRequest, contestId: UUID)

object Actors {
  val system = ActorSystem("BattlePetsArenaSystem")
  val databaseActor = system.actorOf(Props[DatabaseActor], "database")
}