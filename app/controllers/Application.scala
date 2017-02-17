package controllers

import play.api.mvc._
import play.api.libs.json._

// TODO: where should this case class go
case class ContestRequest(petId1: String, petId2: String, contestType: String)

object Application extends Controller {

  implicit val contestRequestReads = Json.reads[ContestRequest]

  // TODO: rm along with view
  def index = Action {
    Ok(views.html.main())
  }

  def contest = Action { request =>

    // Parse the request
    val result: Option[JsResult[ContestRequest]] =
      request.body.asJson.map { jsValue: JsValue =>
        Json.fromJson[ContestRequest](jsValue)
      }

    println(result)

    result match {
      case Some(success: JsSuccess[ContestRequest]) => Ok("ok\n")
      case Some(error: JsError) => BadRequest("Errors: " + JsError.toJson(error).toString())
      case None => BadRequest("br\n")
    }
  }

}
