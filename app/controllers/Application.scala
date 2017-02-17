package controllers

import play.api.mvc._

object Application extends Controller {

  // TODO: rm along with view
  def index = Action {
    Ok(views.html.main())
  }

}
