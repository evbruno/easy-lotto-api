package lotto.jobs

import lotto.api._
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.dsl.DSL.Parse._
import net.ruippeixotog.scalascraper.dsl.DSL._

abstract class HtmlParser extends lotto.api.LottoLogger {

	val fileName: String

	val lottery: Lottery
	val minColumns: Int
	val numbersRange: (Int, Int)
	val prizesTransformer: Line => List[Prize]

	line
	info(s"File to parse: $fileName")

	lazy val browser = new JsoupBrowser()
	lazy val doc = browser.parseFile(fileName)

	type Line = Seq[String]

	private lazy val parseLines: Seq[Line] = {
		val trs = doc >> extractor("tr", asIs)
		trs.map(_ >> "td").filter(_.size > minColumns).map(_.toSeq).toSeq
	}

	def parse: Seq[Result] = {
		parseLines.map { line =>
			val numbers = line.slice(numbersRange._1, numbersRange._2).map(_.toInt).sorted.toIndexedSeq
			val draw = line(0).toInt
			val date = line(1)
			val prizes = prizesTransformer(line)

			Result(draw = draw, numbers = numbers, drawDate = date, prizes = prizes, lottery = lottery)

		}.sortBy(r => r.draw)
	}

}
