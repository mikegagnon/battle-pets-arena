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


//case class MessageInitContest(contestId: UUID)

/**
 * ContestStatus classes
 **************************************************************************************************/
sealed trait ContestStatus {
  val uuid: UUID
}

// TODO: rename, relocate?
case class InProgress(uuid: UUID) extends ContestStatus

case class ContestResult(
    uuid: UUID,
    firstPlacePetName: String,
    secondPlacePetName: String,
    summary: String) extends ContestStatus

sealed trait ContestError extends ContestStatus {
  val code: Int
  val message: String
}

case class ErrorCouldNotFindPet(uuid: UUID, message: String) {
  val code = 1
}

object ErrorServer {
  val code = 2
  val message = "Internal server error"
}

/**
 * DatabaseActor
 **************************************************************************************************/
class DatabaseActor extends Actor {

  val log = Logging(context.system, this)

  var contests = MutableMap[UUID, ContestStatus]()

  def receive = {
    case status: ContestStatus => contests(status.uuid) = status
    case _ => throw new IllegalArgumentException("DatabaseActor received unknown message")
  }

}
