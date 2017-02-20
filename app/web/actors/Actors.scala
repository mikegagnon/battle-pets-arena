package me.michaelgagnon.pets.web.actors

// TODO clean
import akka.actor.Actor
import akka.actor.ActorSystem
import akka.actor.Props
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers._
import akka.stream.ActorMaterializer
import akka.stream.ActorMaterializerSettings
import java.util.UUID
import scala.collection.mutable.{Map => MutableMap}
import scala.concurrent._
import scala.util.{Success, Failure}

import me.michaelgagnon.pets.web.controllers.ContestRequest

// TODO: relocate all these definitions?
// TODO: rename, relocate?
case class ContestFailure(message: String)
// TODO: rename, relocate?
case class ContestResult(contestId: UUID, firstPlacePetName: String, secondPlacePetName: String, summary: String)
// TODO: rename? relocate? encapsulate in DatabaseMessage object?
case class StoreContestResult(contestResult: ContestResult)
case class GetContestResult(contestId: UUID)
case class InitContestResult(contestId: UUID)
// changename to ContestRequestWithId
case class ContestWithId(contest: ContestRequest, contestId: UUID)

object Actors {
  val system = ActorSystem("BattlePetsArenaSystem")
  val databaseActor = system.actorOf(Props[DatabaseActor], "database")
}