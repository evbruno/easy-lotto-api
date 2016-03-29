package loto

import java.io.IOException

import akka.actor.ActorSystem
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.Uri
import akka.http.scaladsl.model.Uri.Query
import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.server.directives.{MiscDirectives, RespondWithDirectives}
import akka.http.scaladsl.server.{Directive0, StandardRoute}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer
import com.typesafe.config.Config
import spray.json.DefaultJsonProtocol

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Left, Right}

case class GoogleOauth2Identity(access_token: String, token_type: String, expires_in: Long, refresh_token: String, id_token: String)

case class GoogleOauth2UserInfo( email: String, name: String, picture: String)

trait GoogleOauth2Protocols extends DefaultJsonProtocol {

	implicit val goIdentityFormat = jsonFormat5(GoogleOauth2Identity.apply)
	implicit val goInfoFormat = jsonFormat3(GoogleOauth2UserInfo.apply)

}

trait GoogleAPI extends GoogleOauth2Protocols {

	import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._

	implicit val system: ActorSystem

	implicit def executor: ExecutionContextExecutor

	implicit val materializer: Materializer

	implicit val config: Config

	import akka.http.scaladsl.Http
	import akka.http.scaladsl.model.{HttpRequest, HttpResponse, _}
	import akka.stream.scaladsl._

	lazy val authHost = s"www.googleapis.com"

	lazy val authUrl = Uri(s"https://$authHost/oauth2/v4/token")
	lazy val infoUrl = Uri(s"https://$authHost/oauth2/v3/userinfo")

	lazy val authConnectionFlow: Flow[HttpRequest, HttpResponse, Any] = Http().outgoingConnectionTls(authHost)

	def authRequest(request: HttpRequest): Future[HttpResponse] = Source.single(request).via(authConnectionFlow).runWith(Sink.head)

	def fetchRequestIdentity(code: String): Future[Either[String, GoogleOauth2Identity]] = {
		val entity=  FormData(Map(
			"code" -> code,
			"client_id" -> config.getString("auth.google.client-id"),
			"client_secret" -> config.getString("auth.google.client-secret"),
			"redirect_uri" -> config.getString("auth.google.redirect-uri"),
			"grant_type" -> "authorization_code"))

		val req = HttpRequest(method = HttpMethods.POST, uri = authUrl, entity = entity.toEntity)

		authRequest(req).flatMap { response =>
			response.status match {
				case StatusCodes.OK => Unmarshal(response.entity).to[GoogleOauth2Identity].map(Right(_))
				case _ => Unmarshal(response.entity).to[String].flatMap { entity =>
					val error = s"Google Auth request failed with status code ${response.status} and entity $entity"
					Future.failed(new IOException(error))
				}
			}
		}
	}
	
	type AccessToken = String
	type FutureGoogleAuth = Future[Either[String, GoogleOauth2UserInfo]]

	def fetchUserInfo(accessToken: AccessToken): FutureGoogleAuth = {
		val params = Query(("alt" -> "json"), ("access_token" -> accessToken))
		val req = HttpRequest(method = HttpMethods.GET, uri = infoUrl.withQuery(params))

		authRequest(req).flatMap { response =>
			response.status match {
				case StatusCodes.OK => Unmarshal(response.entity).to[GoogleOauth2UserInfo].map(Right(_))
				case _ => Unmarshal(response.entity).to[String].flatMap { entity =>
					val error = s"Google Auth request failed with status code ${response.status} and entity $entity"
					Future.failed(new IOException(error))
				}
			}
		}
	}
}

trait GoogleRoutes
	extends MiscDirectives
	with RespondWithDirectives
	with GoogleAPI
	with SprayJsonSupport
	with EnableCORSDirectives {

	import akka.http.scaladsl.model.StatusCodes._
	import akka.http.scaladsl.server.Directives._
	import spray.json._

	implicit val config: Config

	private lazy val googleAuthURL = {
		val params = Map(
			//("client_id" -> "957658579599-3kkqnsl3qcq72m4snif0f808pi0d8cjn.apps.googleusercontent.com"),
			("client_id" -> config.getString("auth.google.client-id")),
			//("redirect_uri" -> "http://lvh.me:9000/auth/google"),
			("redirect_uri" -> config.getString("auth.google.redirect-uri")),
			("response_type" -> "code"),
			("approval_prompt" -> "force"),
			("scope" -> "https://www.googleapis.com/auth/userinfo.email https://www.googleapis.com/auth/userinfo.profile"),
			("state" -> "auth_google"),
			("access_type" -> "offline")
		)
		Uri("https://accounts.google.com/o/oauth2/auth").withQuery(Query(params))
	}

	private def doAuth(code: String): Future[Either[String, (UserInfo, AccessToken)]] = {

		def createUser(user: GoogleOauth2UserInfo, currentToken: String): UserInfo = {
			ApiRepo createUser UserInfo(
				email = user.email,
				name = user.name,
				tokens = currentToken :: Nil,
				pictureUrl = Option(user.picture)
			)
		}

		def addToken(user: UserInfo, token: AccessToken) =
			ApiRepo.insertToken(user.email, token)

		def createOrUpdate(guser: GoogleOauth2UserInfo, identity: GoogleOauth2Identity): UserInfo = {
			ApiRepo findUser (guser.email) match {
				case Some(u) => addToken(u, identity.access_token); u
				case None => createUser(guser, identity.access_token)
			}
		}

		fetchRequestIdentity(code).flatMap { identity =>
			identity match {
				case Left(i) => Future.failed(new RuntimeException(s"i=$i"))
				case Right(id) =>
					fetchUserInfo(id.access_token).flatMap { user =>
						user match {
							case Right(userInfo) =>
								Future.successful(Right((createOrUpdate(userInfo, id), id.access_token)))
							case Left(i) =>
								Future.failed(new RuntimeException(s"Error fetching UserInfo: $i"))
						}
					}
			}
		} recover {
			case x: Throwable => Left(s"Error: $x")
		}
	}

	def setUserCookie(userInfo: UserInfo, accessToken: AccessToken): Directive0 =
			setCookie(
				HttpCookie("X-EL-UserName", value = userInfo.name.replaceAll(" ", "%20"), path = Some("/")),
				HttpCookie("X-EL-UserEmail", value = userInfo.email,  path = Some("/")),
				HttpCookie("X-EL-UserPictureUrl", value = userInfo.pictureUrl.getOrElse(""), path = Some("/")),
				HttpCookie("X-EL-ServerTimestamp", value = new java.util.Date().getTime.toString, path = Some("/")),
				HttpCookie("X-EL-AccessToken", value = accessToken, path = Some("/"))
			)

	def clearUserCookie : Directive0 =
			deleteCookie("X-EL-UserName", path= "/") &
			deleteCookie("X-EL-UserEmail", path= "/") &
			deleteCookie("X-EL-UserPictureUrl", path= "/") &
			deleteCookie("X-EL-ServerTimestamp", path= "/") &
			deleteCookie("X-EL-AccessToken", path= "/")

	def redirOrFail(userInfo: UserInfo, accessToken: AccessToken): StandardRoute =
		if (userInfo.whiteList)
			setUserCookie(userInfo, accessToken) & redirect("/#/welcome", TemporaryRedirect)
		else
			clearUserCookie & complete((Unauthorized, s"User ${userInfo.email} not allowed yet! =("))


	def redirOrFail(authResponse: Either[String, (UserInfo, AccessToken)]): StandardRoute =
		authResponse match {
			case Left(errorMessage) => clearUserCookie & complete((BadRequest, errorMessage))
			case Right((userInfo, accessToken)) => redirOrFail(userInfo, accessToken)
		}

	val googleRoute = ( logRequestResult("easy-loto-auth") & enableCORS ) {

		pathPrefix("auth") {
			(get & path("google" / "url")) {
				complete(Map("auth" -> "google", "url" -> googleAuthURL.toString).toJson)
			} ~
			(get & path("google") & parameter('state) & parameter('code)) { (state, code) =>
				onComplete(doAuth(code)) {
					case Failure(errorMessage) => complete((BadRequest, errorMessage))
					case scala.util.Success(authResponse) => redirOrFail(authResponse)
				}
			}
		}
	}
}
