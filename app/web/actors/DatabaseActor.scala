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

import me.michaelgagnon.pets.contest.ContestResult
import me.michaelgagnon.pets.web.controllers.ContestRequest

/**
 * ContestStatus classes
 **************************************************************************************************/
sealed trait ContestStatus {
  val contestId: UUID
}

case class InProgress(contestId: UUID) extends ContestStatus

case class ContestResultWithId(contestId: UUID, result: ContestResult) extends ContestStatus

sealed trait ContestError extends ContestStatus {
  val code: Int
  val message: String
}

case class ErrorCouldNotFindPet(contestId: UUID, message: String) extends ContestError {
  val code = 1
}

case class ErrorServer(contestId: UUID) extends ContestError {
  val code = 2
  val message = "Internal server error"
} 

case class ErrorAccessPetService(contestId: UUID, petApiHost: String) extends ContestError {
  val code = 3
  val message = "Error accessing Pet service at " + petApiHost
}

case class ErrorResponseFromPetService(contestId: UUID, message: String) extends ContestError {
  val code = 4
}

case class ErrorJsonFromPetService(contestId: UUID) extends ContestError {
  val code = 5
  val message = "Could not parse json from Pet service"
}

/**
 * DatabaseActor
 **************************************************************************************************/
class DatabaseActor extends Actor {

  val log = Logging(context.system, this)

  var contests = MutableMap[UUID, ContestStatus]()

  def receive = {
    case status: ContestStatus => {
      log.info("DatabaseActor received status: " + status)
      contests(status.contestId) = status
    }

    case _ => throw new IllegalArgumentException("DatabaseActor received unknown message")
  }

}
