package me.michaelgagnon.pets.web.controllers

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
import javax.inject._
import play.api._
import play.api.libs.json._
import play.api.mvc._
import scala.concurrent._
import scala.util.{Success, Failure}
import ExecutionContext.Implicits.global

object GetContestId

// TODO: where should this case class go
case class ContestRequest(petId1: String, petId2: String, contestType: String)

case class ContestWithId(contest: ContestRequest, contestId: UUID)

// TODO: comment
class NewContestActor extends Actor {
  val log = Logging(context.system, this)

  final implicit val materializer: ActorMaterializer =
    ActorMaterializer(ActorMaterializerSettings(context.system))

  val http = Http(context.system)

  def receive = {
    case ContestWithId(ContestRequest(petId1, petId2, contestType), contestId) => {
      log.info(s"received newContest: $petId1, $petId2, $contestType, $contestId")

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

@Singleton
class ContestController @Inject() extends Controller {

  val system = ActorSystem("BattlePetsArenaSystem")

  // TODO: rm?
  def launchContest(contestRequest: ContestRequest): Result = {

    val contestId = UUID.randomUUID()

    val contestWithId = ContestWithId(contestRequest, contestId)

    val newContestActor = system.actorOf(Props[NewContestActor])

    newContestActor ! contestWithId

    Created(contestId.toString + "\n")
  }

  // Note: The reason we specify parse.tolerantJson is to avoid Play automatically handling
  // the error case where application/json is missing from the header. We want to avoid this case
  // because Play returns an HTML error message, which is inconsistent with our JSON error messages
  def contest = Action(parse.tolerantJson) { request =>

      implicit val contestRequestReads = Json.reads[ContestRequest]

      // parse the request
      val result: JsResult[ContestRequest] = Json.fromJson[ContestRequest](request.body)

      println(result)

      result match {
        case success: JsSuccess[ContestRequest] => launchContest(success.value)
        case error: JsError => BadRequest("\"Your request is malformed\"\n")
      }
  }

}
