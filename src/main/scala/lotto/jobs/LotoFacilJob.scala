package lotto.jobs

import lotto.api._

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.{Future, ExecutionContext}

object LotoFacilJob extends lotto.api.LottoLogger {

	def job()(implicit ex: ExecutionContext) = Future {
		val downloader = new LotoFacilDownloadZip
		val parser = new LotoFacilHtmlParser(downloader.download)
		val results = parser.parse

		line
		info(s"Results.size: ${results.size}")
		info(s"Results.head: ${results.head}")
		line

		ApiRepo.updateJobExecution(results.size)

		val last: Draw = ApiRepo.lastDraw

		val ret = resultsFor(last, results)

		info(s"Ret: $ret")

		ApiRepo.close

		val emailBody =
			s"""<html><body>
			  |<h1>EasyLottoApi notifier</h1>
			  | <p>Import result:</p>
			  | <strong>$ret</strong>
			  | </body></html>""".stripMargin

		try {
			SendEmail.send(emailBody)
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

	private def resultsFor(last: Draw, results: ArrayBuffer[Result]) = {
		if (last == results.last.draw)
			"Records are on date. Nothing else to do..."
		else {
			results filter (_.draw > last) foreach (ApiRepo.save _)
			if (last == 0) "Save'em all [init] !"
			else s"Save concursos > $last"
		}
	}


}
