import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import lotto.api.{ApiRepoMongo, ApiRoute}

import scala.concurrent.ExecutionContextExecutor

object EasyLottoApiApp extends App with ApiRoute {

	implicit val system = ActorSystem()
	implicit val executor: ExecutionContextExecutor = system.dispatcher
	implicit val materializer = ActorMaterializer()
	implicit val config = ConfigFactory.load()

	override val apiRepo = ApiRepoMongo()

	val logger = Logging(system, getClass)

	val allRoutes = apiRoute

	Http().bindAndHandle(allRoutes, config.getString("http.interface"), config.getInt("http.port"))


}
