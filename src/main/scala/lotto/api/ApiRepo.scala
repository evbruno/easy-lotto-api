package lotto.api

trait ApiRepo {

	def disconnect()

	// Result

	def results: List[Result]

	// Draw

	def lastDraw : Int

	// Bets

	def save(obj: Result)

	def betsFor(draw: Draw): List[Bet]

	def save(bets: Bets): Bets

	def betsFor(user: UserInfo): List[Bets]

	// UserInfo

	def findUser(email: Email): Option[UserInfo]

	def createUser(user: UserInfo): UserInfo

	def insertToken(email: Email, token: String)

	def isAuthorized(email: String, token: String): Boolean

}
