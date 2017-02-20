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

  val database = Actors.databaseActor

  def receive = {
    case contestWithId: ContestWithId => handleNewContest(contestWithId)
    case _ => throw new IllegalArgumentException("NewContestActor received unknown message")
  }

  def requestPets(petId1: String, petId2: String) = {

    // Generate futures for requesting pet data from the Pet API
    // TODO: Seq?
    val List(future1, future2) = List(petId1, petId2)
      .map { petId =>

        val uri = s"$petApiHost/pets/$petId"

        val httpRequest = HttpRequest(uri = uri)
          .withHeaders(RawHeader("X-Pets-Token", petApiToken))

        http.singleRequest(httpRequest)
      }

    // Combine the two futures into one
    for {
      httpResponse1 <- future1
      httpResponse2 <- future2
    } yield (httpResponse1, httpResponse2)
  }

  // TODO: breakup?
  def handleNewContest(contestWithId: ContestWithId) = {

    val ContestWithId(ContestRequest(petId1, petId2, contestType), contestId) = contestWithId

    log.info(s"received newContest: $petId1, $petId2, $contestType, $contestId")

    database ! InProgress(contestId)

    val response: Future[(HttpResponse, HttpResponse)] = requestPets(petId1, petId2)

    response.onComplete {

      case Failure(t) => {
        database ! ErrorAccessPetService(contestId, "Error accessing Pet service at " + petApiHost)
      }

      case Success((resp1, resp2)) => {

        def isFailure(resp: HttpResponse) = resp.status.intValue != 200

        // TODO: report failure details
        if (isFailure(resp1) || isFailure(resp2)) {
          database ! ErrorResponseFromPetService(contestId,
            "Received error response from Pet service at " + petApiHost)
        } else {

        }
      }
    }

    context.stop(self)
  }

}