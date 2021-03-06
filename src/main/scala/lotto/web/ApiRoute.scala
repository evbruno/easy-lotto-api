package lotto.web

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.server.Directives._
import lotto.api.Lottery.{Lotofacil, Lottery, MegaSena}
import lotto.api.{ApiRepo, Lottery, LotteryStatus, LottoLogger}

import scala.concurrent.Future

trait ApiRoute extends ApiProtocols
  with EnableCORSDirectives
  with LottoLogger {

  //import spray.json._
  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._

  implicit val apiRepo: ApiRepo
  implicit val system: ActorSystem
  implicit val logger: LoggingAdapter

  import system.dispatcher

  lazy val staticFilesRoute = path("") {
    getFromResource("www/index.html")
  }

  lazy val apiRoute = lotteriesStatusRoute ~ routeFor(Lotofacil) ~ routeFor(MegaSena)

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
  private lazy val extraDirectives = logRequestResult("easy-lotto-api") & enableCORS

  // public api
  private def routeFor(lottery: Lottery) =
    pathPrefix("api") {
      extraDirectives {
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
      }
    }

  private lazy val lotteriesStatusRoute = path("api" / "lotteries") {
    get {
      complete(lotteriesStatus)
    }
  }

  private lazy val lotteriesStatus =
    Lottery.Enabled.map { l => LotteryStatus(l.toString, l.toSourceName, true) } ++
    Lottery.Disabled.map { l => LotteryStatus(l.toString, l.toSourceName, false) }
}
