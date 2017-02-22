package me.michaelgagnon.pets.web.actors

import akka.actor.Actor
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers._
import akka.stream.ActorMaterializer
import akka.stream.ActorMaterializerSettings
import java.util.UUID
import play.api.Configuration
import play.api.libs.json._
import scala.concurrent._
import scala.concurrent.duration._
import scala.util.{Success, Failure}

import me.michaelgagnon.pets.contest.Games
import me.michaelgagnon.pets.web.controllers.ContestRequest
import me.michaelgagnon.pets.web._

case class Pet(
  id: String,
  name: String,
  strength: Int,
  speed: Int,
  intelligence: Int,
  integrity:Int)

object NewContestActor {

  val database = Actors.databaseActor

  implicit val petReads = Json.reads[Pet]
}

class NewContestActor(config: Configuration)(implicit ec: ExecutionContext) extends Actor {

  import NewContestActor._

  val petApiToken = config.getString("pet.api.token").get
  val petApiHost = config.getString("pet.api.host").get

  val timeout = config.getString("me.michaelgagnon.pets.reqTimeout").get.toInt.seconds

  val log = Logging(context.system, this)

  final implicit val materializer: ActorMaterializer =
    ActorMaterializer(ActorMaterializerSettings(context.system))

  val http = Http(context.system)

  def receive = {
    case contestWithId: ContestWithId => handleNewContest(contestWithId)
    case _ => throw new IllegalArgumentException("NewContestActor received unknown message")
  }

  def petFromJson(json: String, contestId: UUID): Either[ContestError, Pet] = {

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

    val body: Future[String] = httpResponse
      .flatMap { response =>

        // Grab the body future from http response
        response
          .entity
          .toStrict(timeout)
          .map { _.data.utf8String }
      }

    body.map { petFromJson(_, contestId) }

  }

  def runContest(contestId: UUID, pet1: Pet, pet2: Pet, contestType: String): ContestStatus = 
    Games
      .get(contestType)
      .map { game => ContestResultWithId(contestId, game(pet1, pet2)) }
      .getOrElse(ErrorInvalidGame(contestId))

  def handleNewContest(contestWithId: ContestWithId) = {

    log.info("New contest: " + contestWithId)

    val ContestWithId(ContestRequest(petId1, petId2, contestType), contestId) = contestWithId

    database ! PostStatus(InProgress(contestId))

    val pet1: Future[Either[ContestError, Pet]] = getPet(petId1, contestId)
    val pet2: Future[Either[ContestError, Pet]] = getPet(petId2, contestId)
  
    // Join the futures
    val pets: Future[(Either[ContestError, Pet], Either[ContestError, Pet])] = for {
        p1 <- pet1
        p2 <- pet2
      } yield (p1, p2)
    
    pets.onComplete {
        case Failure(t) => database ! PostStatus(ErrorAccessPetService(contestId, petApiHost))
        case Success((Left(error), _)) => database ! PostStatus(error)
        case Success((_, Left(error))) => database ! PostStatus(error)
        case Success((Right(pet1), Right(pet2))) => {
          database ! PostStatus(runContest(contestId, pet1, pet2, contestType))
        }
      }

    context.stop(self)
  }

}