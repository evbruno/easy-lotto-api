package lotto.api

object Lottery {

	def fromSourceName(name: String) : Option[Lottery] = name match {
		case Lotofacil.toSourceName => Some(Lotofacil)
		case MegaSena.toSourceName => Some(MegaSena)
		case _ => None
	}

	def drawnNumbers(lottery: Lottery) : Option[Int] = lottery match {
		case Lotofacil => Some(15)
		case MegaSena => Some(6)
		case _ => None
	}

}

sealed abstract class Lottery {

	lazy val toSourceName = toString.toLowerCase

}

case object Lotofacil extends Lottery


case object MegaSena extends Lottery

// Quina Lotomania Timemania, etc...


