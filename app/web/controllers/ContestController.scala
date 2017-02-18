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
import javax.inject._
import play.api._
import play.api.libs.json._
import play.api.mvc._
import scala.concurrent._
import ExecutionContext.Implicits.global

// TODO: where should this case class go
case class ContestRequest(petId1: String, petId2: String, contestType: String)

// TODO: comment
class NewContestActor extends Actor {
  val log = Logging(context.system, this)

  final implicit val materializer: ActorMaterializer =
    ActorMaterializer(ActorMaterializerSettings(context.system))

  val http = Http(context.system)

  def receive = {
    case ContestRequest(petId1, petId2, contestType) => {
      log.info(s"received newContest: $petId1, $petId2, $contestType")

      val httpRequest = HttpRequest(uri = "https://wunder-pet-api-staging.herokuapp.com/pets")
        .withHeaders(RawHeader("X-Pets-Token", ""))

      val fut = http.singleRequest(httpRequest)

      for (result <- fut) {
        log.info(result.toString)
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

    val newContestActor = system.actorOf(Props[NewContestActor])

    newContestActor ! contestRequest

    Created("ok\n")
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
