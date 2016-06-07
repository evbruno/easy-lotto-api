package lotto.jobs

class MegaSenaHtmlParser(val fileName: String) extends HtmlParser {

	val minColumns = 17

	val numbersRange = (2, 8)

	val prizesTransformer = (line: Line) =>
		(6 -> line(12)) ::
		(5 -> line(14)) ::
		(4 -> line(16)) :: Nil

}

//object Spike extends App {
//
//	val p = new lotto.jobs.MegaSenaHtmlParser("/tmp/d_megasc.htm")
//	val r = p.parse
//
//	println(r)
//
//}
