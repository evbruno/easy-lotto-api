import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import lotto.api.ApiRepoMongo
import lotto.web.ApiRoute

object EasyLottoApiApp extends App with ApiRoute {

	implicit val system = ActorSystem()
	implicit val materializer = ActorMaterializer()
	implicit val config = ConfigFactory.load()

	implicit val apiRepo = ApiRepoMongo()

	val logger: LoggingAdapter = Logging(system, getClass)

	val allRoutes = staticFilesRoute ~ apiRoute

	Http().bindAndHandle(allRoutes, config.getString("http.interface"), config.getInt("http.port"))

}
