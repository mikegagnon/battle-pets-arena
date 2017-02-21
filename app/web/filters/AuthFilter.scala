package me.michaelgagnon.pets.web.filters

// Taken from https://github.com/wunderteam/battle-pets-api/blob/master/app/web/filters/AuthFilter.scala

import javax.inject.Inject

import akka.stream.Materializer
import play.api.Configuration
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

// TODO: investigate
object AuthFilter {
  // if you put this here there is ever only 1 instance instead of 1/class.
  val CONTEST_TOKEN = "Contest-Token"
}

class AuthFilter @Inject()(config: Configuration)(implicit val mat: Materializer, ec: ExecutionContext) extends Filter {

  def apply(nextFilter: RequestHeader => Future[Result])
           (requestHeader: RequestHeader): Future[Result] = {

    val result: Option[Boolean] = for {
      token <- requestHeader.headers.get(AuthFilter.CONTEST_TOKEN)
      configuredToken <- config.getString("me.michaelgagnon.pets.token")
    } yield token.matches(configuredToken)

    val unauthorized = Future.successful(Results.Unauthorized)

    result.map(matches => if (matches) nextFilter(requestHeader) else unauthorized).getOrElse(unauthorized)
  }
}


