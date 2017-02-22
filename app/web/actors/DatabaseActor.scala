package me.michaelgagnon.pets.web.actors

import akka.actor.Actor
import akka.event.Logging
import java.util.UUID
import scala.collection.mutable.{Map => MutableMap}

import me.michaelgagnon.pets.web.{ContestStatus, NoStatus}

case class PostStatus(conestStatus: ContestStatus)

case class RequestStatus(contestId: UUID)

class DatabaseActor extends Actor {

  var contests = MutableMap[UUID, ContestStatus]()

  def receive = {
    case PostStatus(contestStatus) => {
      contests(contestStatus.contestId) = contestStatus
    }

    case RequestStatus(contestId) => {
      sender ! contests.getOrElse(contestId, NoStatus(contestId))
    }

    case _ => throw new IllegalArgumentException("DatabaseActor received unknown message")
  }

}
