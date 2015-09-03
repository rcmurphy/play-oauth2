package play.contrib.oauth2

import com.netaporter.uri.dsl._
import play.api.Application
import play.api.mvc.{Request, WrappedRequest}

case class RequestWithOAuthTokens[A](tokens: Map[String, String], request: Request[A]) extends WrappedRequest[A](request) {

  def withToken(token: (String, String)): RequestWithOAuthTokens[A] = {
    new RequestWithOAuthTokens(tokens + token, request)
  }
}

object RequestWithOAuthTokens {
  def apply[A](request: Request[A], token: (String, String)) = request match {
    case r: RequestWithOAuthTokens[A] => new RequestWithOAuthTokens[A](r.tokens + token, r.request)
    case r => new RequestWithOAuthTokens[A](Map() + token, r)
  }
}

case class OAuth2App(
  clientId: String,
  clientSecret: String,
  redirectUrlBase: String,
  tokenUrl: String
) {
  def withScopes(scopes: Seq[String]) = OAuth2AppWithScopes(this, scopes)
}

object OAuth2App {
  def fromConfig(appName: String)(implicit application: Application): Option[OAuth2App] = {
    for {
      clientId <- application.configuration.getString(s"$appName.client.id")
      clientSecret <- application.configuration.getString(s"$appName.client.secret")
      redirectUrl <- application.configuration.getString(s"$appName.url.redirect")
      tokenUrl <- application.configuration.getString(s"$appName.url.access-token")
    } yield {
      new OAuth2App(clientId, clientSecret, redirectUrl, tokenUrl)
    }
  }

}

case class OAuth2AppWithScopes(
  oauth2App: OAuth2App,
  scopes: Seq[String]
) {
  val clientId: String = oauth2App.clientId
  val clientSecret: String = oauth2App.clientSecret
  val redirectUrlBase: String = oauth2App.redirectUrlBase
  val tokenUrl: String = oauth2App.tokenUrl

  def redirectUri(callbackUrl: String, state: String): String = {
    val url = redirectUrlBase ?
      ("response_type" -> "code") &
      ("client_id" -> clientId) &
      ("redirect_uri" -> callbackUrl) &
      ("scope" -> scopes.mkString(" ")) &
      ("state" -> state)
    url.toString()
  }

}

case class OAuth2Token(token: String)
