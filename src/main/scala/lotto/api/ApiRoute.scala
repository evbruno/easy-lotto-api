package lotto.api

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.coding.Gzip
import akka.http.scaladsl.server.Directives._

import scala.concurrent.Future

trait ApiRoute extends BetProtocols
	with LottoLogger {

	import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
//	import spray.json._

	implicit val apiRepo : ApiRepo
	implicit val system: ActorSystem
	implicit val logger: LoggingAdapter

	import system.dispatcher

	val staticFilesRoute = path("") {
			getFromResource("www/index.html")
	}

	val apiRoute = routeFor(Lotofacil) ~ routeFor(MegaSena)

	private val pageSize = 5

	private def resultsFor(lottery: Lottery, page: Int = 1) = Future {
		val skip = (page - 1) * pageSize
		logger.info("Results for {}, skipping {} results", lottery, skip)
		apiRepo.results(lottery).drop(skip).take(pageSize)
	}

	private def resultFor(lottery: Lottery, draw: Int) = Future {
		logger.info("Finding draw {} for {}", draw, lottery)
		apiRepo.findResult(lottery, draw)
	}

	// private lazy val extraDirectives = logRequestResult("easy-lotto-api") & encodeResponseWith(Gzip)
	private lazy val extraDirectives = logRequestResult("easy-lotto-api")

	private def routeFor(lottery: Lottery) =
		pathPrefix("api") { extraDirectives {
			get {
				path(lottery.toSourceName ~ (PathEnd | Slash)) {
					onSuccess(resultsFor(lottery)) { results =>
						complete(results)
					}
				} ~
				path(lottery.toSourceName / IntNumber) { page =>
					onSuccess(resultsFor(lottery, page)) { results =>
						complete(results)
					}
				} ~
				path(lottery.toSourceName / ("games" | "draws") / IntNumber) { game =>
					onSuccess(resultFor(lottery, game)) {
						case Some(r) => complete(r)
						case None => reject
					}
				}
			} // get
		} }

}
