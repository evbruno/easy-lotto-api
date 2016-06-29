package lotto.web

import lotto.api.{Bets, Lottery, Result}
import spray.json.{JsString, _}

trait ApiProtocols extends DefaultJsonProtocol {

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

	implicit val resultFormat: RootJsonFormat[Result] = jsonFormat5(Result.apply)
	implicit val betsFormat: RootJsonFormat[Bets] = jsonFormat7(Bets.apply)

}