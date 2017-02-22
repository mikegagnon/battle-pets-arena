package me.michaelgagnon.pets.web.actors

import akka.actor.Actor
import akka.event.Logging
import java.util.UUID
import scala.collection.mutable.{Map => MutableMap}

import me.michaelgagnon.pets.web.{ContestStatus, NoStatus}

case class PostStatus(conestStatus: ContestStatus)

case class RequestStatus(contestId: UUID)

class DatabaseActor extends Actor {

  val log = Logging(context.system, this)

  var contests = MutableMap[UUID, ContestStatus]()

  def receive = {
    case PostStatus(contestStatus) => {
      log.info("Post status to db: " + contestStatus)
      contests(contestStatus.contestId) = contestStatus
    }

    case RequestStatus(contestId) => {
      val result = contests.getOrElse(contestId, NoStatus(contestId))
      log.info("Request contest status. Contest status: " + result)
      sender ! result
    }

    case _ => throw new IllegalArgumentException("DatabaseActor received unknown message")
  }

}
