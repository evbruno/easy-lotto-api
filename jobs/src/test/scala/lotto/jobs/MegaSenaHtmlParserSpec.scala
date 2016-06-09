package lotto.jobs

import org.scalatest._

class MegaSenaHtmlParserSpec extends FlatSpec with Matchers {

	"The MegaSena" should "have 1824 draws" in {
		val file = getClass.getResource("/d_megasc.htm").getFile
		val subject = new MegaSenaHtmlParser(file)
		val parsed = subject.parse

		val first = parsed.head
		first.draw should be(1)
		first.drawDate should be("11/03/1996")
		first.numbers should contain allOf(4, 5, 30, 33, 41, 52)

		first.prizes(0) should be((6, "0,00"))
		first.prizes(1) should be((5, "39.158,92"))
		first.prizes(2) should be((4, "330,21"))

		val last = parsed.last
		last.draw should be(1824)
		last.drawDate should be("04/06/2016")
		last.numbers should contain allOf(5, 6, 12, 19, 30, 60)

		last.prizes(0) should be((6, "0,00"))
		last.prizes(1) should be((5, "14.170,67"))
		last.prizes(2) should be((4, "387,97"))
	}


}
