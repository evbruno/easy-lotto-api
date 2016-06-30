package lotto.api

import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.TypeImports
import com.mongodb.casbah.{MongoClientURI, MongoDB}

object ApiRepoMongo {

  // eg:  mongodb://<dbuser>:<dbpassword>@mongolob.url:AAA/DATABASE
  // val DEFAULT_URI = scala.util.Properties.envOrElse("MONGOLAB_URI", "mongodb://localhost:27017/easy-lotto")
  val DEFAULT_URI = scala.util.Properties.envOrElse("MONGOLAB_URI", "mongodb://local-docker:27017/easy-lotto")

  def apply(uri: String = DEFAULT_URI): ApiRepoMongo = {
    val clientURI = MongoClientURI(uri)
    val client = MongoClient(clientURI)
    val db = client(clientURI.database.get)

    require(db.getStats() != null)

    new ApiRepoMongo(client, db)
  }

}

class ApiRepoMongo private(val mongoClient: MongoClient, val db: MongoDB) extends ApiRepo with LottoLogger {

  def disconnect() = {
    info("API DISCONNECT	")
    mongoClient.close()
  }

  private val dbResults = db("results")
  private val dbBets = db("bets")
  private val dbUsers = db("users")

  private def toResult(doc: TypeImports.DBObject, lottery: Lottery) =
    Result(draw = doc.as[Int]("draw"),
      numbers = doc.as[List[Int]]("numbers"),
      drawDate = doc.as[String]("draw-date"),
      prizes = extractPrizes(doc),
      lottery = lottery)

  def results(lottery: Lottery): List[Result] = {
    val query = ("lottery" $eq lottery.toSourceName)
    val cursor = dbResults.find(query).sort(MongoDBObject("draw" -> -1))

    (for (doc <- cursor) yield
      toResult(doc, lottery)
      ).toList
  }

  def findResult(lottery: Lottery, draw: Draw): Option[Result] = {
    val query = $and(
      ("lottery" -> lottery.toSourceName),
      ("draw" -> draw)
    )

    dbResults findOne (query) match {
      case None => None
      case Some(doc) => Some(toResult(doc, lottery))
    }
  }

  private def extractPrizes(doc: DBObject): List[Prize] = {
    val res = doc.as[BasicDBList]("prizes").map { p =>
      (p.asInstanceOf[BasicDBList].get(0).asInstanceOf[Int],
        p.asInstanceOf[BasicDBList].get(1).asInstanceOf[String])
    }
    res.toList
  }

  def betsFor(userEmail: String, lottery: Lottery): List[Bets] = {
    val ownerQuery = $or(("owner" -> userEmail), ("fellows" -> userEmail))
    val lotteryQuery = "lottery" $eq lottery.toSourceName
    val query = ownerQuery ++ lotteryQuery

    def keyOr[A](doc: DBObject, key: String, default: => A)(implicit m: Manifest[A]): A =
      if (doc.contains(key)) doc.as[A](key)
      else default

    def basicListToSeqOfNumbers(doc: MongoDBList) =
      doc.map(_.asInstanceOf[BasicDBList].toList).asInstanceOf[Seq[IndexedSeq[Int]]]

    val bets = for {
      doc <- dbBets.find(query)
    } yield Bets(owner = doc.as[String]("owner"),
      lottery = lottery,
      from = doc.as[Int]("from"),
      to = doc.as[Int]("to"),
      fellows = keyOr(doc, "fellows", Seq.empty),
      numbers = basicListToSeqOfNumbers(doc.as[MongoDBList]("numbers")))

    bets.toList
  }

  def save(bets: Bets): Bets = {
    val doc = MongoDBObject(
      "owner" -> bets.owner,
      "from" -> bets.from,
      "to" -> bets.to,
      "fellows" -> bets.fellows,
      "numbers" -> bets.numbers,
      "lottery" -> bets.lottery.toSourceName
    )

    dbBets insert doc

    val objId = doc.get("_id")
    bets.copy(id = Some(objId.toString))
  }

  private def extractNumbers(doc: BasicDBList): Numbers = {
    doc.map(_.asInstanceOf[Int]).toList
  }

  def lastDraw(lottery: Lottery): Draw = {
    val query = "lottery" $eq lottery.toSourceName

    val fields = MongoDBObject("draw" -> 1)
    val orderBy = MongoDBObject("draw" -> -1)

    val x = dbResults.findOne(query, fields, orderBy)

    x match {
      case Some(k) => k.getAs[Int]("draw").get
      case _ => 0
    }
  }

  def save(obj: Result) {
    dbResults insert MongoDBObject(
      "draw" -> obj.draw,
      "numbers" -> obj.numbers,
      "lottery" -> obj.lottery.toSourceName,
      "draw-date" -> obj.drawDate,
      "prizes" -> obj.prizes
    )
  }

  // UserInfo

  def findUser(email: Email): Option[UserInfo] = {

    val f = (doc: MongoDBObject, key: String) =>
      if (doc.contains(key)) Option(doc.as[String](key))
      else None

    dbUsers findOne MongoDBObject("email" -> email) match {
      case None => None
      case Some(doc) =>
        Some(UserInfo(
          email = doc.as[String]("email"),
          name = doc.as[String]("name"),
          whiteList = true,
          tokens = doc.as[List[String]]("tokens"),
          pictureUrl = f(doc, "picture")
        ))
    }
  }

  def createUser(user: UserInfo): UserInfo = {
    val doc = MongoDBObject(
      "name" -> user.name,
      "email" -> user.email,
      "picture" -> user.pictureUrl
    )

    if (!user.tokens.isEmpty)
      doc += ("tokens" -> user.tokens)

    dbUsers insert doc

    user.copy(whiteList = true)
  }

  def insertToken(email: Email, token: String) = {
    val where = MongoDBObject("email" -> email)

    //val what = MongoDBObject("$push" -> MongoDBObject("tokens" -> token))
    val what = $push("tokens" -> token)

    dbUsers update(where, what)
  }

  def isAuthorized(email: String, token: String): Boolean =
    !dbUsers.findOne(MongoDBObject("email" -> email, "tokens" -> token)).isEmpty


}
