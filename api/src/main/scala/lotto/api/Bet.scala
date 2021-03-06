package lotto.api

import lotto.api.Lottery.Lottery

case class Result(draw: Int,
                  drawDate: String,
                  numbers: Numbers,
                  prizes: List[Prize],
                  lottery: Lottery) {

  assert(draw > 0, s"Invalid draw: ${draw}")
  // FIXME: remove Option murderer !
  assert(numbers.toSet.size == Lottery.drawnNumbers(lottery).get, s"Invalid number combination: ${numbers.mkString(", ")}")

}


// FIXME: update lottery
//case class Bet(numbers: Numbers,
//               owner: Option[String] = None) {
//
//	assert(numbers.size == 15, s"Invalid number combination: ${numbers}")
//
//}

case class Bets(owner: String,
                lottery: Lottery,
                from: Draw,
                to: Draw,
                numbers: Seq[Numbers],
                fellows: Seq[Email] = Seq.empty,
                id: Option[_ID] = None)
