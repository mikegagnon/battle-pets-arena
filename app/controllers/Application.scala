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

    /*
    val parsed = request.body.asJson.map { jsValue =>
      val petId1 = (jsValue \ "pet-id-1")
      val petId2 = (jsValue \ "pet-id-2")
      val contestType = (jsValue \ "contest-type")
      (petId1, petId2, contestType)
    }*/

  val contestRequest: Option[JsResult[ContestRequest]] = request.body.asJson.map { jsValue : JsValue =>
      Json.fromJson[ContestRequest](jsValue)
  }



    println(contestRequest)

    Ok(views.html.main())
  }

}
