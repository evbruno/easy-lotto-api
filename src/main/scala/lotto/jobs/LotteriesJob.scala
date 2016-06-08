package lotto.jobs

import com.typesafe.config.{ConfigFactory, Config}
import lotto.api.{SendEmail, ApiRepo, ApiRepoMongo, LottoLogger}
import pl.project13.scala.rainbow.Rainbow

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class LotteriesJob extends LottoLogger {

	import scala.concurrent.ExecutionContext.Implicits.global

	implicit val apiRepo: ApiRepo = ApiRepoMongo()

	val init = System.currentTimeMillis

	val config = ConfigFactory.load()
	val downloadTimeout = config.getInt("jobs.download-data-timeout")
	val sendEmailTimeout = config.getInt("jobs.send-email-timeout")

	info(s"Download timeout: $downloadTimeout min, send email timeout: $sendEmailTimeout min.")

	val jobsResults: List[JobResult] = Await.result(downloadDataFromLotteriesJob, downloadTimeout.minutes)

	import Rainbow._

	info(s"Importing data is done, took ${System.currentTimeMillis - init} millis".blue)

	apiRepo.disconnect()

	Await.result(sendEmailJob(jobsResults), sendEmailTimeout.minutes)

	info(s"Whole job is done, took ${System.currentTimeMillis - init} millis".blue)

	// Local API

	def downloadDataFromLotteriesJob = Future.sequence(
		List(
				new MegaSenaJob().job(),
				new LotofacilJob().job()
		)
	)

	def sendEmailJob(results: List[JobResult]) = Future {
		val html = toEmailHtml(results)
		info(s">>>>> EMAIL: \n\t$html \n")

		if (SendEmail.canSendEmail)
			SendEmail.send(html)
		else
			warn("Email couldn't be sent")

	}

	def toEmailHtmlLine(jobResult: JobResult) = jobResult match {

			case UpToDate(lottery) =>
				s"<li><strong>${lottery}</strong> is up to date</li>"

			case FewInserts(lottery, draws) =>
				s"<li><strong>${lottery}</strong> imported the draws: <strong>${draws.mkString(", ")}</strong></li>"

			case FullImport(lottery, total) =>
				s"<li><strong>${lottery}</strong> was fully imported, total of <strong>${total}</strong> records</li>"
		}


	def toEmailHtml(results: List[JobResult])  = {
		val jobsDetails = results map(toEmailHtmlLine) mkString("\n\t\t")

		s"""<html>
		   | <body>
		   | 	<h1>EasyLottoApi notifier</h1>
		   |	<h3>Import result:</h3>
		   | 	<ul>
		   |		${jobsDetails}
		   | 	</ul>
		   | </body>
		   | </html>""".stripMargin
	}

}
