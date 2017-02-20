package me.michaelgagnon.pets.web.controllers

// TODO: Clean
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
import scala.collection.mutable.{Map => MutableMap}
import scala.concurrent._
import scala.util.{Success, Failure}
import ExecutionContext.Implicits.global

import me.michaelgagnon.pets.web.actors.{Actors, ContestWithId, NewContestActor}

// TODO: rm?
object GetContestId

// TODO: where should this case class go
// TODO: organize
case class ContestRequest(petId1: String, petId2: String, contestType: String)


@Singleton
class ContestController @Inject() extends Controller {

/*  val system = ActorSystem("BattlePetsArenaSystem")

  val databaseActor = system.actorOf(Props[DatabaseActor], "database")
*/
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

          val newContestActor = Actors.system.actorOf(Props(new NewContestActor()))

          newContestActor ! contestWithId

          Created(contestId.toString + "\n")
        }
      }
  }

}
