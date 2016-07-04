package lotto.jobs

import lotto.api.Lottery.MegaSena

class MegaSenaHtmlParser(val fileName: String) extends HtmlParser {

  override val lottery = MegaSena

  override val minColumns = 17

  override val numbersRange = (2, 8)

  override val prizesTransformer = (line: Line) =>
        (6 -> line(12)) ::
        (5 -> line(14)) ::
        (4 -> line(16)) :: Nil

}
