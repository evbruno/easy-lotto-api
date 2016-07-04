package lotto.api

import lotto.api
import lotto.api.Lottery.{Lotofacil, MegaSena}
import org.scalatest._

class BetHitsSpec extends FlatSpec with Matchers {

	"A Lotofacil Result" should "have size 15 and positive draw" in {
		val result = Result(draw = 1, numbers = (1 to 15).toIndexedSeq, drawDate = "", prizes = Nil, lottery = Lotofacil)
		result.numbers.size should be(15)
		result.draw should be > 0
	}

	"A Lotofacil Result" should "have size == 15" in {
		an[AssertionError] should be thrownBy Result(draw = 1, numbers = (1 to 16).toIndexedSeq, drawDate = "", prizes = Nil, lottery = Lotofacil)
	}

	"A MegaSena Result" should "have size == 6" in {
		val thrown0 = the[AssertionError] thrownBy Result(draw = 1, numbers = (1 to 5).toIndexedSeq, drawDate = "", prizes = Nil, lottery = MegaSena)
		thrown0.getMessage should equal("assertion failed: Invalid number combination: 1, 2, 3, 4, 5")

		import api._

		val repeatedNumbers = List(1, 1, 2, 2, 3, 3)
		val thrown1 = the[AssertionError] thrownBy Result(draw = 1, numbers = repeatedNumbers, drawDate = "", prizes = Nil, lottery = MegaSena)
		thrown1.getMessage should equal("assertion failed: Invalid number combination: 1, 1, 2, 2, 3, 3")
	}

	"A Lotofacil Result" should "have positive draw" in {
		an[AssertionError] should be thrownBy Result(draw = -1, numbers = (1 to 15).toIndexedSeq, drawDate = "", prizes = Nil, lottery = Lotofacil)
	}

}
