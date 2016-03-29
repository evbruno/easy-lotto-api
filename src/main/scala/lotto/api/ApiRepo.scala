package lotto.api

import com.mongodb.casbah.Imports._

object ApiRepo {

	private val LOTO = "lotofacil"
	private val MEGA = "mega-sena"

	// eg:  mongodb://<dbuser>:<dbpassword>@mongolob.url:AAA/DATABASE
	val uri = scala.util.Properties.envOrElse("MONGOLAB_URI", "mongodb://localhost:27017/lotofacil")
	private val mongoClientURI = MongoClientURI(uri)

	private val mongoClient = MongoClient(mongoClientURI)
	private val db = mongoClient(mongoClientURI.database.get)

	private val dbResults = db("results")
	private val dbBets = db("bets")
	private val dbJobUpdates = db("jobs_updates")
	private val dbUsers = db("users")
	private val dbWhiteList = db("users_white_list")

	def close() = mongoClient.close()

	def results: List[Result] = {
		val cursor = dbResults.find().sort(MongoDBObject("draw" -> -1))
		(for (doc <- cursor) yield
			Result(draw = doc.as[Int]("draw"),
				numbers = doc.as[List[Int]]("numbers"),
				drawDate = doc.as[String]("draw-date"),
				prizes = extractPrizes(doc))
		).toList
	}

	private def extractPrizes(doc: DBObject) : List[Prize] = {
		val res = doc.as[BasicDBList]("prizes").map { p =>
			(p.asInstanceOf[BasicDBList].get(0).asInstanceOf[Int],
				p.asInstanceOf[BasicDBList].get(1).asInstanceOf[String])
		}
		res.toList
	}

	def betsFor(draw: Draw) : List[Bet] = {
		val query = ("from" $lte draw) ++
					("to" $gte draw) ++
					("source" $eq LOTO)

		(
			for {
				doc <- dbBets.find(query)
				numbers <- doc.as[List[BasicDBList]]("numbers")
			} yield Bet(numbers = extractNumbers(numbers), owner = Some(doc.as[String]("key")))
		).toList
	}

	def betsFor(user: UserInfo) : List[Bets] = {
		val query = $or(
					("owner" -> user.email),
					("fellows" -> user.email)
				)

		def keyOr[A](doc: DBObject, key: String, default: => A)(implicit m: Manifest[A]) : A =
			if (doc.contains(key)) doc.as[A](key)
			else default

		(
			for {
				doc <- dbBets.find(query)
			} yield Bets(owner = doc.as[String]("owner"),
						from = doc.as[Int]("from"),
						to = doc.as[Int]("to"),
						fellows = keyOr(doc, "fellows", Seq.empty),
						numbers = doc.as[Seq[Numbers]]("numbers"))
		).toList
	}


//	case class Bets(owner: String,
//					from: Draw,
//					to: Draw,
//					fellows: Seq[Email],
//					numbers: Seq[Numbers])

	def save(bets: Bets) : Bets = {
		val doc = MongoDBObject(
			"owner" -> bets.owner,
			"from" -> bets.from,
			"to" -> bets.to,
			"fellows" -> bets.fellows,
			"numbers" -> bets.numbers
		)

		dbBets insert doc

		val objId = doc.get("_id")
		bets.copy(id = Some(objId.toString))
	}

	private def extractNumbers(doc: BasicDBList) : Numbers = {
		doc.map(_.asInstanceOf[Int]).toList
	}

	def lastDraw: Int = {
		val query = MongoDBObject() // All documents
		val fields = MongoDBObject("draw" -> 1)
		val orderBy = MongoDBObject("draw" -> -1)

		val x = dbResults.findOne(query, fields, orderBy)

		x match {
			case Some(k) => k.getAs[Int]("draw").get
			case _ => 0
		}
	}

	def updateJobExecution(totalParsed: Int) {
		dbJobUpdates insert	MongoDBObject(
				"when" -> new java.util.Date,
				"totalParsed" -> totalParsed,
				"currentSize" -> dbJobUpdates.size
			)
	}

	def save(obj: Result) {
		dbResults insert MongoDBObject(
			"draw" -> obj.draw,
			"numbers" -> obj.numbers,
			"source" -> LOTO,
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
					whiteList = isWhiteList(email),
					tokens = doc.as[List[String]]("tokens"),
					pictureUrl = f(doc, "picture")
				))
		}
	}

	def createUser(user: UserInfo) : UserInfo = {
		val doc = MongoDBObject(
			"name" -> user.name,
			"email" -> user.email,
			"picture" -> user.pictureUrl
		)

		if (!user.tokens.isEmpty)
			doc += ("tokens" -> user.tokens)

		dbUsers insert doc

		user.copy(whiteList = isWhiteList(user.email))
	}

	private def isWhiteList(email: Email) : Boolean = {
		val q = MongoDBObject("email" -> email)
		dbWhiteList findOne (q) match {
			case None => false
			case Some(doc) => true
		}
	}

	def insertToken(email: Email, token: String) = {
		val where = MongoDBObject("email" -> email)

		//val what = MongoDBObject("$push" -> MongoDBObject("tokens" -> token))
		val what = $push("tokens" -> token)

		dbUsers update (where, what)
	}

	def isAuthorized(email: String, token: String) : Boolean =
		! dbUsers.findOne(MongoDBObject("email" -> email, "tokens" -> token)).isEmpty

	// workspaces

	protected[api] def whiteList(email: String) =
		dbWhiteList.insert(MongoDBObject(
			"email" -> email
		))

}

//object ApiRepoApp extends App {
//////	val r = ApiRepo.results
//////	println(s"Last: ${r.head}")
//////	println(s"First: ${r.last}")
////
//	val nb = ApiRepo.save(Bets("Duh", 1,2, Seq(), Seq()))
//	println(nb)
//	ApiRepo.close
//}