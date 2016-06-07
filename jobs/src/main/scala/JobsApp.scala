import lotto.api.LottoLogger
import lotto.jobs.LotoFacilJob

import scala.concurrent._
import scala.concurrent.duration._
import scala.util.{Failure, Success}

object JobsApp extends App with LottoLogger {

	import scala.concurrent.ExecutionContext.Implicits.global

	val init = System.currentTimeMillis

	val job = LotoFacilJob.job()

	Await.ready(job, 10.minutes)

	job onComplete {
		case Failure(t) =>
			error("Error running job", t)
		case Success(_) =>
			info(s"Job's done, took ${System.currentTimeMillis - init} millis.")
	}

}
