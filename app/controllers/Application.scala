package controllers

import javax.inject._
import play.api._
import play.api.mvc._
import play.api.libs.json._

// TODO: where should this case class go
case class ContestRequest(petId1: String, petId2: String, contestType: String)




@Singleton
class Application @Inject() extends Controller {

  // Note: The reason we specify parse.tolerantJson is to avoid Play automatically handling
  // the error case where application/json is missing from the header. We want to avoid this case
  // because Play returns an HTML error message, which is inconsistent with our JSON error messages
  def contest = Action(parse.tolerantJson) { request =>

      implicit val contestRequestReads = Json.reads[ContestRequest]

      // parse the request
      val result: JsResult[ContestRequest] = Json.fromJson[ContestRequest](request.body)

      println(result)

      result match {
        case success: JsSuccess[ContestRequest] => Ok("ok\n")
        case error: JsError => BadRequest("\"Your request is malformed\"\n")
      }

  }

}
