package lotto.jobs

import lotto.api._
import lotto.jobs.JobTemplate.HtmlParserFactory

import scala.concurrent.{ExecutionContext, Future}

object JobTemplate {

	type HtmlParserFactory = String => HtmlParser
}

abstract class JobTemplate(implicit val api: ApiRepo) extends LottoLogger {

	val downloader: FileDownloader
	val parserFactory: HtmlParserFactory
	val lottery: Lottery

	def job()(implicit ex: ExecutionContext) : Future[JobResult] = Future {

		val tmp = downloader.download
		val parser = parserFactory(tmp)
		val results = parser.parse

		line
		info(s"$prefix parsed results.size: ${results.size}")
		info(s"$prefix parsed results.head: ${results.head}")
		line

		val last: Draw = api.lastDraw(lottery)

		val jobResult = resultsFor(last, results)

		info(s"$prefix job will return $jobResult")

		jobResult
	}

	private lazy val prefix = s"$lottery at ${Thread.currentThread().getName} : "

	private def resultsFor(last: Draw, results: Seq[Result]) =
		if (last == results.last.draw)
			UpToDate(lottery)											//"Records are on date. Nothing else to do..."
		else {
			val newResults = results filter (_.draw > last)
			newResults foreach (api.save _)
			if (last == 0)
				FullImport(lottery, results.size)  						//"Save'em all [init] !"
			else {
				val newDraws = newResults map (_.draw)
				FewInserts(lottery, newDraws)							//s"Save draws > $last"
			}

		}


}
