package lotto.jobs

class LotofacilHtmlParser(val fileName: String) extends HtmlParser {

	val minColumns = 30

	val numbersRange = (2, 17)

	val prizesTransformer = (line: Line) =>
		(15 -> line(25)) ::
		(14 -> line(26)) ::
		(13 -> line(27)) ::
		(12 -> line(28)) ::
		(11 -> line(29)) :: Nil

}
