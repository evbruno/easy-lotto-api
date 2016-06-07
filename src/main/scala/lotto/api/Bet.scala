package lotto.api

import spray.json._

trait BetProtocols extends DefaultJsonProtocol {

	implicit val betFormat = jsonFormat2(Bet.apply)
	implicit val resultFormat: RootJsonFormat[Result] = jsonFormat4(Result.apply)
	implicit val betsFormat: RootJsonFormat[Bets] = jsonFormat6(Bets.apply)

}

case class Result(draw: Int, drawDate: String, numbers: Numbers, prizes: List[Prize]) {
	assert(draw > 0, s"Invalid draw: ${draw}")
	// FIXME : LotteryType ?
	//assert(numbers.size == 15, s"Invalid number combination: ${numbers}")
}

case class Bet(numbers: Numbers, owner: Option[String] = None) {
	assert(numbers.size == 15, s"Invalid number combination: ${numbers}")
}

case class Bets(owner: String,
				from: Draw,
				to: Draw,
			   	fellows: Seq[Email],
				numbers: Seq[Numbers],
			   	id: Option[_ID] = None)
