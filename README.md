[![Build Status](https://travis-ci.org/rcmurphy/play-oauth2.svg?branch=master)](https://travis-ci.org/rcmurphy/play-oauth2)

# Example Usage

## OAuth2 Controller Definition
```scala

@ImplementedBy(classOf[ExampleOAuthControllerImpl])
trait ExampleOAuthController extends OAuth2Controller {

}

trait ExampleOAuthConfig extends OAuth2Config {
  override val callbackRoute: (Option[String], Option[String]) => Call = controllers.routes.ExampleOAuthController.callback _
  override val service: String = "example"
}

class ExampleOAuthControllerImpl extends ExampleOAuthController with OAuth2ControllerImpl with ExampleOAuthConfig {
  override implicit val application: Application = Play.current

}

case class ExampleOAuthWrapper(scopes: String*) extends OAuth2Wrapper with ExampleOAuthConfig

```

## Associated Route

```
GET        /example/oauth-callback            controllers.ExampleOAuthController.callback(code: Option[String], state: Option[String])
```

## Using the OAuth2 Wrapper
```scala
class MyControllerImpl extends Controller with MyController {

  val exampleOAuthAction = ExampleOAuthWrapper("some_scope")

  override def doSomething: EssentialAction = exampleOAuthAction { request =>
    val token = request.tokens("example")
    ???
  }
}

```

## Configuration
```scala

example.client.id=YOUR_ID
example.client.secret=YOUR_SECRET
example.url.redirect="https://example.com/oauth/authorize"
example.url.access-token="https://example.com/oauth/token"
```
