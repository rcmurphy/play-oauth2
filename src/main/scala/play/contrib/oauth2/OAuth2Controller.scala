package play.contrib.oauth2

import play.api.mvc.{Action, Controller, EssentialAction}
import play.api.{Application, Logger, Play}
import play.api.libs.concurrent.Execution.Implicits._
import scala.concurrent.Future

trait OAuth2Controller {
  def callback(codeOpt: Option[String] = None, stateOpt: Option[String] = None): EssentialAction
}

trait OAuth2ControllerImpl extends Controller with OAuth2Controller {
  config: OAuth2Config =>

  implicit val application: Application = Play.current

  private val logger = Logger(getClass)

  lazy val oauth2 = new OAuth2(service)

  def callback(codeOpt: Option[String] = None, stateOpt: Option[String] = None) = Action.async { implicit request =>
    (for {
      code <- codeOpt
      state <- stateOpt
      oauthState <- request.session.get(s"$service-oauth-state")
    } yield {
      if (state == oauthState) {
        oauth2.getToken(code, callbackRoute).map { accessToken =>
          Redirect("/").withSession(s"$service-oauth-token" -> accessToken)
        }.recover {
          case ex: IllegalStateException => Unauthorized(ex.getMessage)
        }
      } else {
        Future.successful(BadRequest(s"Invalid $service login"))
      }
    }).getOrElse(Future.successful(BadRequest("No parameters supplied")))
  }

  /*def success() = Action.async { request =>
    implicit val app = Play.current
    request.session.get(s"$service-oauth-token").fold(Future.successful(Unauthorized(s"Invalid Token for $service"))) { authToken =>

    }
  }*/

}
