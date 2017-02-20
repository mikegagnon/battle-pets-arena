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
import play.api.libs.json._
import scala.collection.mutable.{Map => MutableMap}
import scala.concurrent._
import scala.util.{Success, Failure}


// TODO
import scala.concurrent.duration._


import me.michaelgagnon.pets.web.controllers.ContestRequest

// TODO: relocate?
case class Pet(
  id: String,
  name: String,
  strength: Int,
  speed: Int,
  intelligence: Int,
  integrity:Int)

// TODO: comment
// TODO: move statics to companion object
class NewContestActor(config: Configuration)(implicit ec: ExecutionContext) extends Actor {

  val log = Logging(context.system, this)

  val petApiToken = config.getString("pet.api.token").get
  val petApiHost = config.getString("pet.api.host").get

  final implicit val materializer: ActorMaterializer =
    ActorMaterializer(ActorMaterializerSettings(context.system))

  val http = Http(context.system)

  val database = Actors.databaseActor

  implicit val petReads = Json.reads[Pet]

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

  // TODO: rename
  def getPet(httpResponse: HttpResponse, contestId: UUID): Either[ErrorJsonFromPetService, Pet] = {
    
    // TODO: move
    val timeout = 300.millis

    val bs: Future[String] = httpResponse.entity
      .toStrict(timeout)
      .map { _.data.utf8String }
      .map { body: String =>
        println(body)
        body
      }
    
    //val s: Future[String] = bs.map(_.utf8String) // if you indeed need a `String

    /*val petResult: JsResult[Pet] = Json.fromJson[Pet](httpResponse.value)

    petResult match {
      case error: JsError =>
        Left(ErrorJsonFromPetService(contestId, "Could not parse JSON response from Pet service"))

      case success: JsSuccess[Pet] => Right(success.value)
    }*/

    Left(ErrorJsonFromPetService(contestId, "Could not parse JSON response from Pet service"))
  }

  def handlePetResponse(
      response: Future[(HttpResponse, HttpResponse)],
      contestId: UUID,
      contestType: String) =
    response.onComplete {

      case Failure(t) => {
        database ! ErrorAccessPetService(contestId,
          "Error accessing Pet service at " + petApiHost)
      }

      case Success((resp1, resp2)) => {

        def isFailure(resp: HttpResponse) = resp.status.intValue != 200

        // TODO: report failure details
        if (isFailure(resp1) || isFailure(resp2)) {
          database ! ErrorResponseFromPetService(contestId,
            "Received error response from Pet service at " + petApiHost)
        } else {

          val pet1: Either[ErrorJsonFromPetService, Pet] = getPet(resp1, contestId)
          val pet2: Either[ErrorJsonFromPetService, Pet] = getPet(resp2, contestId)

          println(pet1)
          println(pet2)

        }
      }
  }

  def handleNewContest(contestWithId: ContestWithId) = {

    val ContestWithId(ContestRequest(petId1, petId2, contestType), contestId) = contestWithId

    log.info(s"received newContest: $petId1, $petId2, $contestType, $contestId")

    database ! InProgress(contestId)

    val response: Future[(HttpResponse, HttpResponse)] = requestPets(petId1, petId2)
    
    handlePetResponse(response, contestId, contestType)

    context.stop(self)
  }

}