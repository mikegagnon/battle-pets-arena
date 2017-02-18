package controllers

import akka.actor.Actor
import akka.actor.ActorSystem
import akka.actor.Props
import akka.event.Logging
import javax.inject._
import play.api._
import play.api.libs.json._
import play.api.mvc._

// TODO: where should this case class go
case class ContestRequest(petId1: String, petId2: String, contestType: String)

// TODO: comment
class NewContestActor extends Actor {
  val log = Logging(context.system, this)

  def receive = {
    case ContestRequest(petId1, petId2, contestType) =>
      log.info(s"received newContest: $petId1, $petId2, $contestType")
    case _ => log.info("received unknown message")
  }
}

@Singleton
class Application @Inject() extends Controller {

  val system = ActorSystem("BattlePetsArenaSystem")

  // TODO: rm?
  def launchContest(contestRequest: ContestRequest): Result = {

    val newContestActor = system.actorOf(Props[NewContestActor])

    newContestActor ! contestRequest

    Ok("ok\n")
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
