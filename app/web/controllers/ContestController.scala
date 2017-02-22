package me.michaelgagnon.pets.web.controllers

//import akka.actor.Actor
//import akka.actor.ActorSystem
import akka.actor.Props
import scala.concurrent.duration._
import akka.pattern.ask
import akka.util.Timeout
import java.util.UUID
import javax.inject._
import play.api._
import play.api.libs.json._
import play.api.mvc._
import scala.concurrent._
import ExecutionContext.Implicits.global

import me.michaelgagnon.pets.web.actors._
import me.michaelgagnon.pets.web.ContestStatus

case class ContestRequest(petId1: String, petId2: String, contestType: String)

@Singleton
class ContestController @Inject()(config: Configuration) extends Controller {

  // Note: The reason we specify parse.tolerantJson is to avoid Play automatically handling
  // the error case where application/json is missing from the header. We want to avoid this case
  // because Play returns an HTML error message, which is inconsistent with our JSON error messages
  def contest = Action(parse.tolerantJson) { request =>

      implicit val contestRequestReads = Json.reads[ContestRequest]

      // parse the request
      val result: JsResult[ContestRequest] = Json.fromJson[ContestRequest](request.body)

      println(result)

      result match {
        case error: JsError => BadRequest("\"Your request is malformed\"\n")
        case success: JsSuccess[ContestRequest] => {

          val contestRequest = success.value

          val contestId = UUID.randomUUID()

          val contestWithId = ContestWithId(contestRequest, contestId)

          val newContestActor = Actors.system.actorOf(Props(new NewContestActor(config)))

          newContestActor ! contestWithId

          Created("\"" +contestId.toString + "\"")
        }
      }
  }

  def result(contestIdString: String) = Action { request =>

    val contestId: Option[UUID] = try {
        Some(UUID.fromString(contestIdString))
      } catch {
        case e: IllegalArgumentException => None
      }

    contestId
      .map { id: UUID =>
        // TODO: put timeout in configuration
        implicit val timeout = Timeout(5 seconds)
        val future = Actors.databaseActor ? RequestStatus(id)
        val result = Await.result(future, timeout.duration).asInstanceOf[ContestStatus]
        val resultJson = result.toJson.toString

        println(resultJson)
        Ok(resultJson)
      }
      .getOrElse(BadRequest("\"Invalid contestId\""))
  }

}
