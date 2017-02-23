package me.michaelgagnon.pets.web

import java.util.UUID
import play.api.libs.json._

import me.michaelgagnon.pets.contest.ContestResult
import me.michaelgagnon.pets.contest.Games

// Each ContestStatus class represents the status of a contest. e.g. is the contest in progress?
// Has it completed? Was there an error?
//
// The ContestsStatus objects are stored in the database, then when a result is requested, the
// the object is converted to JSON and returned.

sealed trait ContestStatus {
  val contestId: UUID
  val message: String
  val code: Int
  def toJson: JsValue = JsObject(Seq(
      "code" -> JsNumber(code),
      "message" -> JsString(message)
    ))
}

case class InProgress(contestId: UUID) extends ContestStatus {
  val code = 1
  val message = "Contest in progress"
}

case class ContestResultWithId(contestId: UUID, result: ContestResult) extends ContestStatus {
  val code = 2
  val message = "Contest complete"

  override def toJson: JsValue = {
    val result1 = Seq(
        "firstPlace" -> JsString(result.firstPlace.name),
        "secondPlace" -> JsString(result.secondPlace.name)
      )

    val result2 =
      result1 ++ result.summary.map{ "summary" -> JsString(_) }.toSeq

    JsObject(Seq(
      "code" -> JsNumber(code),
      "message" -> JsString(message),
      "result" -> JsObject(result2)
    ))
  }
}

sealed trait ContestError extends ContestStatus

case class ErrorCouldNotFindPet(contestId: UUID) extends ContestError {
  val code = -1
  val message = "Could not find pet"
}

case class ErrorServer(contestId: UUID) extends ContestError {
  val code = -2
  val message = "Internal server error"
} 

case class ErrorAccessPetService(contestId: UUID, petApiHost: String) extends ContestError {
  val code = -3
  val message = "Error accessing Pet service at " + petApiHost
}

case class ErrorJsonFromPetService(contestId: UUID) extends ContestError {
  val code = -4
  val message = "Could not parse json from Pet service"
}

case class ErrorInvalidGame(contestId: UUID) extends ContestError {
  val code = -5
  val message = "Error: you specified an invalid contest. Available contests: " + 
    Games.keys.mkString(", ")
}

case class NoStatus(contestId: UUID) extends ContestError {
  val code = -6
  val message = "Error: the contestId does not match any contests"
}
