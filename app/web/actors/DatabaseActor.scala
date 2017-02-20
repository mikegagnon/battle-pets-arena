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



// TODO: own file
class DatabaseActor extends Actor {

  val log = Logging(context.system, this)

  // contests(contestId) is left implies the contest failed (e.g. if could not find petId)
  // contests(contestId) is right None implies contest is in process
  // contests(contestId) is right Some contains contest result
  var contests = MutableMap[UUID, Either[ContestFailure, Option[ContestResult]]]()

  def receive = {

    case InitContestResult(contestId: UUID) => {
      if (contests.contains(contestId)) {
        throw new IllegalArgumentException("Cannot InitContestResult because db already has " +
          contestId.toString)
      }

      contests(contestId) = Right(None)

      log.info("InitContestResult(" + contestId.toString +")")
    }

    // store the result of a contest
    // TODO: document/enforce preconditions
    case StoreContestResult(contestResult) => {

      contests(contestResult.contestId) = Right(Some(contestResult))

      log.info(contestResult.contestId.toString,
        contestResult.firstPlacePetName,
        contestResult.secondPlacePetName,
        contestResult.summary)
    }

    // retrieve the result of a contest
    case GetContestResult(contestId: UUID) => {

      // TODO. Also note, contestId might not exist in db, in which case we should reply with
      // ContestFailure

      log.info(contestId.toString)
    }
  }

}
