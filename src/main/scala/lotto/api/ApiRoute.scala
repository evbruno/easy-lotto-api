package lotto.api

import akka.actor.ActorSystem
import akka.http.scaladsl.coding.Gzip
import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.server.{Directive0, Directive, AuthorizationFailedRejection, Directive1}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.directives.{Credentials, MiscDirectives, RespondWithDirectives}
import com.typesafe.config.Config

import scala.concurrent.Future

trait ApiRoute extends BetProtocols
	with MiscDirectives
	with RespondWithDirectives
	with LottoLogger
	with EnableCORSDirectives {

	import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
	import spray.json._

	//import scala.concurrent.ExecutionContext.Implicits.global

	implicit val system: ActorSystem

	implicit val config: Config

	import system.dispatcher

	// FIXME
	// val apiRepo = ApiRepoMongo()
	val apiRepo : ApiRepo

	def betsF(d: Int) = Future {
		println(s"getting bets for $d")
		apiRepo.betsFor(d)
	}

	def resultsF = Future {
		apiRepo.results(Lotofacil)
	}

//	def findUser(s: String) : Directive1[Any] = ???

	val authDirective0 : Directive1[String] = cookie("X-EL-AccessToken").flatMap {
		case x : HttpCookiePair => provide("ck:" + x.value)
		case _ => reject
	}

	val authDirective : Directive1[(Email, String)] =
		(cookie("X-EL-AccessToken") & cookie("X-EL-UserEmail")).tflatMap {
			case (HttpCookiePair(_, accessToken), HttpCookiePair(_, email)) =>
				if (apiRepo.isAuthorized(email, accessToken)) provide((email, accessToken))
				else reject(AuthorizationFailedRejection)
			case _ => reject
		}

	//private lazy val extraDirectives = logRequestResult("easy-lotto-api")
	private lazy val extraDirectives = logRequestResult("easy-lotto-api") & encodeResponseWith(Gzip)

	val apiRoute =
		path("") {
			complete("Hello there (=")
		} ~ (extraDirectives & pathPrefix("api")) {

			(get & path("ping")) {
				complete { s"pong at ${new java.util.Date()}" }
			} ~
				(get & path("lotofacil") & encodeResponse) {
					onSuccess(resultsF) { r => complete(r.take(10).toJson)	}
			} ~
				(get & path("lotofacil" / IntNumber / "bets")) { drawNumber =>
					onSuccess(betsF(drawNumber)) { h => complete(h.toJson) }
			} ~
				(post & path("lotofacil" / "bets") &  entity(as[Bets])) { bets =>
					authDirective { authResp =>
						val nb = apiRepo.save(bets)
						complete(Map("ok" -> nb).toJson)
					}
				} ~
				(post & path("lotofacil" / "results") &  entity(as[Result])) { r =>
					complete(Map("result inflate" -> r).toJson)
				}
	}

}
