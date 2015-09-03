package play.contrib.oauth2

import java.util.UUID

import play.api.http.{HeaderNames, MimeTypes}
import play.api.libs.ws.WS
import play.api.mvc._
import play.api.{Application, Logger, Play}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class OAuth2(service: String)(implicit application: Application = Play.current) {
  lazy val oauth2App = OAuth2App.fromConfig(service).get

  private val logger = Logger(getClass)

  /*  def getAuthorizationUrl(redirectUri: String, scopes: Seq[String], state: String): String = {
      val baseUrl = application.configuration.getString(s"$service.url.redirect").get
      baseUrl.format(serviceAuthId, redirectUri, scopes.mkString(","), state)
    }*/

  def getToken(code: String, callbackRoute: (Option[String], Option[String]) => Call)(implicit request: Request[Any]): Future[String] = {
    val tokenResponse = WS.url(oauth2App.tokenUrl)
      .withHeaders(HeaderNames.ACCEPT -> MimeTypes.JSON)
      .withQueryString(
        "code" -> code,
        "client_id" -> oauth2App.clientId,
        "client_secret" -> oauth2App.clientSecret,
        "redirect_uri" -> callbackRoute(None, None).absoluteURL(),
        "grant_type" -> "authorization_code",
        "access_type" -> "offline"
      )
      .post(Results.EmptyContent())

    tokenResponse.flatMap { response =>
      (response.json \ "access_token").asOpt[String].fold(Future.failed[String](new IllegalStateException(s"Invalid Response from $service token URI"))) { accessToken =>
        Future.successful(accessToken)
      }
    }
  }
}

trait OAuth2Config {

  val service: String

  val callbackRoute: (Option[String], Option[String]) => Call
}

trait OAuth2Wrapper extends ActionBuilder[RequestWithOAuthTokens] with ActionRefiner[Request, RequestWithOAuthTokens] with Results {
  config: OAuth2Config =>

  val scopes: Seq[String]

  private val logger = Logger(getClass)

  override def refine[A](request: Request[A]): Future[Either[Result, RequestWithOAuthTokens[A]]] = Future {
    val oauth2 = new OAuth2(service)
    val callbackUrl = callbackRoute(None, None).absoluteURL()(request)
    val state = UUID.randomUUID().toString + "/" + request.path // random confirmation string
    val redirectUrl = oauth2.oauth2App.withScopes(scopes).redirectUri(callbackUrl, state)
    request.session.get(s"$service-oauth-token").map { token =>
      logger.info(s"Creating request wuth oauth token: $service -> $token")
      RequestWithOAuthTokens(request, service -> token)
    }.toRight {
      TemporaryRedirect(redirectUrl).withSession(s"$service-oauth-state" -> state)
    }
  }
}
