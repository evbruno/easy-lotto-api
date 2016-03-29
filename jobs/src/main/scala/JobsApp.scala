import lotto.api.LottoLogger
import lotto.jobs.LotoFacilJob

import scala.concurrent._
import scala.concurrent.duration._

object JobsApp extends App with LottoLogger {

	import scala.concurrent.ExecutionContext.Implicits.global

	val init = System.currentTimeMillis

	val lotoFacil = LotoFacilJob.job()

	Await.ready(lotoFacil, 10.minutes)

	info(s"Job's done, took ${System.currentTimeMillis - init} millis.")

}
