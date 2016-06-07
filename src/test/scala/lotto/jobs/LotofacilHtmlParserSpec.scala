package lotto.jobs

import org.scalatest._

class LotofacilHtmlParserSpec extends FlatSpec with Matchers {

	"The LotoFacil" should "have 1370 draws" in {
		val file = getClass.getResource("/D_LOTFAC.HTM").getFile
		val subject = new LotofacilHtmlParser(file)
		val parsed = subject.parse

		val first = parsed.head
		first.draw should be(1)
		first.drawDate should be("29/09/2003")
		first.numbers should contain allOf(2, 3, 5, 6, 9, 10, 11, 13, 14, 16, 18, 20, 23, 24, 25)

		first.prizes(0) should be((15, "49.765,82"))
		first.prizes(1) should be((14, "689,84"))
		first.prizes(2) should be((13, "10,00"))
		first.prizes(3) should be((12, "4,00"))
		first.prizes(4) should be((11, "2,00"))

		val last = parsed.last
		last.draw should be(1370)
		last.drawDate should be("03/06/2016")
		last.numbers should contain allOf(2, 4, 5, 7, 8, 11, 13, 14, 17, 18, 20, 21, 22, 24, 25)

		last.prizes(0) should be((15, "173.084,30"))
		last.prizes(1) should be((14, "786,77"))
		last.prizes(2) should be((13, "20,00"))
		last.prizes(3) should be((12, "8,00"))
		last.prizes(4) should be((11, "4,00"))
	}

}
