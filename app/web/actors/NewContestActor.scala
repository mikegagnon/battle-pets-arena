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

import me.michaelgagnon.pets.contest.Games
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


  def petFromJson(json: String, contestId: UUID): Either[ContestError, Pet] = {

    implicit val petReads = Json.reads[Pet]

    // parse the request
    val result: JsResult[Pet] = Json.fromJson[Pet](Json.parse(json))

    result match {
      case error: JsError => Left(ErrorJsonFromPetService(contestId))
      case success: JsSuccess[Pet] => Right(success.value)
    }

  }

  def getPet(petId: String, contestId: UUID): Future[Either[ContestError, Pet]] = {

    val uri = s"$petApiHost/pets/$petId"

    val httpRequest = HttpRequest(uri = uri)
      .withHeaders(RawHeader("X-Pets-Token", petApiToken))

    val httpResponse: Future[HttpResponse]= http.singleRequest(httpRequest)

    // TODO: move?
    val timeout = 300.millis

    val responseBody: Future[String] =
      httpResponse
        .flatMap { response =>

          // This little bit of code grabs the body from http response
          response
            .entity
            .toStrict(timeout)
            .map { _.data.utf8String }

        }

      
    responseBody
      // TODO: _
      .map { jsonString =>
        val pet = petFromJson(jsonString, contestId)
        println(pet)
        pet
      }

  }

  def runContest(contestId: UUID, pet1: Pet, pet2: Pet, contestType: String): ContestStatus = 
    Games
      .get(contestType)
      .map { game =>
        ContestResultWithId(contestId, game(pet1, pet2))
      }
      .getOrElse(ErrorInvalidGame(contestId))

  def handleNewContest(contestWithId: ContestWithId) = {

    val ContestWithId(ContestRequest(petId1, petId2, contestType), contestId) = contestWithId

    log.info(s"received newContest: $petId1, $petId2, $contestType, $contestId")

    database ! InProgress(contestId)

    val pet1: Future[Either[ContestError, Pet]] = getPet(petId1, contestId)
    val pet2: Future[Either[ContestError, Pet]] = getPet(petId2, contestId)
  
    // Join the futures
    val pets: Future[(Either[ContestError, Pet], Either[ContestError, Pet])] = for {
        p1 <- pet1
        p2 <- pet2
      } yield (p1, p2)
    
    println(pets)

    pets.onComplete {
        case Failure(t) => database ! ErrorAccessPetService(contestId, petApiHost)
        case Success((Left(error), _)) => database ! error
        case Success((_, Left(error))) => database ! error
        case Success((Right(pet1), Right(pet2))) => {
          database ! runContest(contestId, pet1, pet2, contestType)
        }
      }

    context.stop(self)
  }

}