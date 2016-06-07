package lotto.jobs

import lotto.api._
import net.ruippeixotog.scalascraper.browser.JsoupBrowser

import scala.collection.mutable.ArrayBuffer

class LotofacilHtmlParser(fileName: String) extends lotto.api.LottoLogger {

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
			if (tds.size >= 30)
				lines += tds.toSeq
		}

		lines
	}

	def parse: ArrayBuffer[Result] = {
		parseLines.map { line =>
			val numbers = line.slice(2, 17).map(_.toInt).sorted.toIndexedSeq
			val draw = line(0).toInt
			val date = line(1)
			val prizes =
				(15 -> line(25)) ::
				(14 -> line(26)) ::
				(13 -> line(27)) ::
				(12 -> line(28)) ::
				(11 -> line(29)) :: Nil

			Result(draw = draw, numbers = numbers, drawDate = date, prizes = prizes)
		}.sortBy(r => r.draw)
	}

}

//object Spike extends App {
//
//	val p = new lotto.jobs.LotoFacilHtmlParser("/tmp/D_LOTFAC.HTM")
//	val r = p.parse
//
//	println(r)
//
//}
