package lotto.api

import spray.json._

// FIXME: Move to routes
trait BetProtocols extends DefaultJsonProtocol {

	implicit object LotteryJsonFormat extends RootJsonFormat[Lottery] {

		def write(l: Lottery) = JsObject("name" -> JsString(l.toSourceName))

		def read(value: JsValue) = {
			value.asJsObject.getFields("name") match {

				case Seq(JsString(name)) => Lottery.fromSourceName(name) match {
					case Some(l) => l
					case _ => throw new DeserializationException("Lottery expected")
				}

				case _ => throw new DeserializationException("Lottery expected")
			}
		}
	}

	implicit val betFormat = jsonFormat2(Bet.apply)
	implicit val resultFormat: RootJsonFormat[Result] = jsonFormat5(Result.apply)
	implicit val betsFormat: RootJsonFormat[Bets] = jsonFormat6(Bets.apply)

}

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
case class Bet(numbers: Numbers, owner: Option[String] = None) {
	assert(numbers.size == 15, s"Invalid number combination: ${numbers}")
}

case class Bets(owner: String,
				from: Draw,
				to: Draw,
				fellows: Seq[Email],
				numbers: Seq[Numbers],
				id: Option[_ID] = None)
