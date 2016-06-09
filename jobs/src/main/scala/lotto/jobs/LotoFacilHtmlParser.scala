package lotto.jobs

import lotto.api.Lotofacil

class LotofacilHtmlParser(val fileName: String) extends HtmlParser {

	override val lottery = Lotofacil

	override val minColumns = 30

	override val numbersRange = (2, 17)

	override val prizesTransformer = (line: Line) =>
		(15 -> line(25)) ::
		(14 -> line(26)) ::
		(13 -> line(27)) ::
		(12 -> line(28)) ::
		(11 -> line(29)) :: Nil

}
