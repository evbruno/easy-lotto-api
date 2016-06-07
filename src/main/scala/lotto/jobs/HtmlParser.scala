package lotto.jobs

import lotto.api._
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.dsl.DSL.Parse._
import net.ruippeixotog.scalascraper.dsl.DSL._

import scala.collection.mutable.ArrayBuffer

abstract class HtmlParser extends lotto.api.LottoLogger {

	val fileName: String

	val minColumns: Int
	val numbersRange: (Int, Int)
	val prizesTransformer: Line => List[Prize]

	line
	info(s"File to parse: $fileName")

	lazy val browser = new JsoupBrowser()
	lazy val doc = browser.parseFile(fileName)

	type Line = Seq[String]
	type LineBuff = scala.collection.mutable.ArrayBuffer[Line]

	private def parseLines: LineBuff = {

		val trs = doc >> extractor("tr", asIs)
		info(s"Count 'trs': ${trs.size}")

		var lines = new LineBuff()

		trs.foreach { tr =>
			val tds: Iterable[String] = tr >> "td"
			if (tds.size >= minColumns)
				lines += tds.toSeq
		}

		lines
	}

	def parse: ArrayBuffer[Result] = {
		parseLines.map { line =>
			val numbers = line.slice(numbersRange._1, numbersRange._2).map(_.toInt).sorted.toIndexedSeq
			val draw = line(0).toInt
			val date = line(1)
			val prizes = prizesTransformer(line)

			Result(draw = draw, numbers = numbers, drawDate = date, prizes = prizes)
		}.sortBy(r => r.draw)
	}

}
