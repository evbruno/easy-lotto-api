package lotto.jobs

import lotto.api._

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.{Future, ExecutionContext}

object LotofacilJob extends lotto.api.LottoLogger {

	def job()(implicit ex: ExecutionContext) = Future {
		val apiRepo : ApiRepo = ApiRepoMongo()

		val downloader = new LotofacilDownloadZip
		val parser = new LotofacilHtmlParser(downloader.download)

		val results = parser.parse

		line
		info(s"Results.size: ${results.size}")
		info(s"Results.head: ${results.head}")
		line

		val last: Draw = apiRepo.lastDraw

		val ret = resultsFor(apiRepo, last, results)

		info(s"Ret: $ret")

		apiRepo.disconnect()

		val emailContent = emailBody(ret)

		info("####@@@@@@@ ")
		info(emailContent)
		info("####@@@@@@@ ")

		// FIXME : try-catch evil
		try {
			SendEmail.send(emailContent)
		} catch {
			case x : Throwable => {
				info("Error sending email")
				x.printStackTrace()
			}
		}

		line
		info(s"The End.")
		line
	}

	private def resultsFor(apiRepo: ApiRepo, last: Draw, results: ArrayBuffer[Result]) =
		if (last == results.last.draw)
			"Records are on date. Nothing else to do..."
		else {
			results filter (_.draw > last) foreach (apiRepo.save _)
			if (last == 0) "Save'em all [init] !"
			else s"Save concursos > $last"
		}

	private def emailBody(ret: String) =
		s"""<html>
			| <body>
			| 	<h1>EasyLottoApi notifier</h1>
			| 	<p>Import result:</p>
			| 	<strong>$ret</strong>
			| </body>
			| </html>""".stripMargin


}
