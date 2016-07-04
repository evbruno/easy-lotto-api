package lotto.web

import lotto.api.Lottery.{Lottery, MegaSena}
import lotto.api._

class ApiRepoMem extends ApiRepo {

	override def disconnect(): Unit = ???

	override def results(lottery: Lottery): List[Result] = List(
		Result(2, "2/2/2222", List(1, 2, 3, 4, 5, 7), List((5, "$ 8,00"), (6, "$ 9,00")), MegaSena),
		Result(1, "1/1/1111", List(1, 2, 3, 4, 5, 6), List((5, "$ 5,00"), (6, "$ 6,00")), MegaSena)
	)

	override def betsFor(userEmail: String, lottery: Lottery): List[Bets] = ???

	override def isAuthorized(email: String, token: String): Boolean = ???

	override def createUser(user: UserInfo): UserInfo = ???

	override def insertToken(email: Email, token: String): Unit = ???

	override def lastDraw(lottery: Lottery): Draw = ???

	override def save(obj: Result): Unit = ???

	override def save(bets: Bets): Bets = ???

	override def findUser(email: Email): Option[UserInfo] = ???

	override def findResult(lottery: Lottery, draw: Draw): Option[Result] =
		Some(Result(2, "2/2/2222", List(1, 2, 3, 4, 5, 7), List((5, "$ 8,00"), (6, "$ 9,00")), MegaSena))
}