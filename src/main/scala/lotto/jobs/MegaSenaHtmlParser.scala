package lotto.jobs

import lotto.api._
import net.ruippeixotog.scalascraper.browser.JsoupBrowser

import scala.collection.mutable.ArrayBuffer

class MegaSenaHtmlParser(fileName: String) extends lotto.api.LottoLogger {

	import net.ruippeixotog.scalascraper.dsl.DSL.Parse._
	import net.ruippeixotog.scalascraper.dsl.DSL._

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
			if (tds.size >= 17) {
				lines += tds.toSeq
			}
		}

		lines
	}

	def parse: ArrayBuffer[Result] = {
		parseLines.map { line =>
			val numbers = line.slice(2, 8).map(_.toInt).sorted.toIndexedSeq
			val draw = line(0).toInt
			val date = line(1)
			val prizes =
				(6 -> line(12)) ::
				(5 -> line(14)) ::
				(4 -> line(16)) ::  Nil

			Result(draw = draw, numbers = numbers, drawDate = date, prizes = prizes)
		}.sortBy(r => r.draw)
	}

}

//object Spike extends App {
//
//	val p = new lotto.jobs.MegaSenaHtmlParser("/tmp/d_megasc.htm")
//	val r = p.parse
//
//	println(r)
//
//}
