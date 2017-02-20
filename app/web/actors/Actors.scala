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

// TODO: own file
// TODO: comment
//class NewContestActor()(implicit ec: ExecutionContext) extends Actor {
class NewContestActor()(implicit ec: ExecutionContext) extends Actor {

  val log = Logging(context.system, this)

  final implicit val materializer: ActorMaterializer =
    ActorMaterializer(ActorMaterializerSettings(context.system))

  val http = Http(context.system)

  def receive = {
    case ContestWithId(ContestRequest(petId1, petId2, contestType), contestId) => {
      log.info(s"received newContest: $petId1, $petId2, $contestType, $contestId")

      context.actorOf(Props[DatabaseActor], "database") ! contestId

      // Generate futures for requesting pet data from the Pet API
      val List(future1, future2) = List(petId1, petId2)
        .map { petId =>

          // TODO: take uri as a configuration element
          val uri = s"https://wunder-pet-api-staging.herokuapp.com/pets/$petId"

          val apiKey = ""

          val httpRequest = HttpRequest(uri = uri).withHeaders(RawHeader("X-Pets-Token", apiKey))

          http.singleRequest(httpRequest)
        }

      // Combine the two futures into one
      val response: Future[(HttpResponse, HttpResponse)] = for {
        httpResponse1 <- future1
        httpResponse2 <- future2
      } yield (httpResponse1, httpResponse2)

      response.onComplete {
        case Success((resp1, resp2)) => println(resp1, resp2)
        case Failure(t) => println(t)
      }

      context.stop(self)
    }
    case _ => throw new IllegalArgumentException("NewContestActor received unknown message")
  }
}

object Actors {
  val system = ActorSystem("BattlePetsArenaSystem")
  val databaseActor = system.actorOf(Props[DatabaseActor], "database")
}