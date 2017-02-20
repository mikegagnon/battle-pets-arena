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
import play.api.Configuration
import scala.collection.mutable.{Map => MutableMap}
import scala.concurrent._
import scala.util.{Success, Failure}


import me.michaelgagnon.pets.web.controllers.ContestRequest

// TODO: own file
// TODO: comment
//class NewContestActor()(implicit ec: ExecutionContext) extends Actor {
class NewContestActor(config: Configuration)(implicit ec: ExecutionContext) extends Actor {

  val log = Logging(context.system, this)

  val petApiToken = config.getString("pet.api.token").get
  val petApiHost = config.getString("pet.api.host").get

  final implicit val materializer: ActorMaterializer =
    ActorMaterializer(ActorMaterializerSettings(context.system))

  val http = Http(context.system)


  def receive = {
    case contestWithId: ContestWithId => handleNewContest(contestWithId)
    case _ => throw new IllegalArgumentException("NewContestActor received unknown message")
  }


  def handleNewContest(contestWithId: ContestWithId) = {
      
      val ContestWithId(ContestRequest(petId1, petId2, contestType), contestId) = contestWithId

      log.info(s"received newContest: $petId1, $petId2, $contestType, $contestId")

      context.actorOf(Props[DatabaseActor], "database") ! contestId

      // Generate futures for requesting pet data from the Pet API
      val List(future1, future2) = List(petId1, petId2)
        .map { petId =>

          val uri = s"$petApiHost/pets/$petId"

          val httpRequest = HttpRequest(uri = uri)
            .withHeaders(RawHeader("X-Pets-Token", petApiToken))

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

}