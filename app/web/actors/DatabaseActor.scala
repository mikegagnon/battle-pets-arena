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
import me.michaelgagnon.pets.contest.Games
import me.michaelgagnon.pets.web.controllers.ContestRequest
import me.michaelgagnon.pets.web.{ContestStatus, NoStatus}

import play.api.libs.json._



// TODO: comment
case class PostStatus(conestStatus: ContestStatus)

case class RequestStatus(contestId: UUID)

/**
 * DatabaseActor
 **************************************************************************************************/
class DatabaseActor extends Actor {

  val log = Logging(context.system, this)

  var contests = MutableMap[UUID, ContestStatus]()

  def receive = {
    case PostStatus(contestStatus) => {
      log.info("DatabaseActor received status: " + contestStatus)
      contests(contestStatus.contestId) = contestStatus
    }

    case RequestStatus(contestId) => {
      sender ! contests.getOrElse(contestId, NoStatus(contestId))
    }

    case _ => throw new IllegalArgumentException("DatabaseActor received unknown message")
  }

}
