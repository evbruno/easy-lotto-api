package lotto.api

import com.github.simplyscala.{MongoEmbedDatabase, MongodProps}
import lotto.api.Lottery.MegaSena
import org.scalatest.{BeforeAndAfter, FunSuite, Inside, Matchers}

class ApiRepoMongoSuite extends FunSuite
  with MongoEmbedDatabase
  with BeforeAndAfter
  with Matchers
  with Inside {

  var mongoProps: MongodProps = null

  var api: ApiRepoMongo = _

  before {
    mongoProps = mongoStart(12345) // by default port = 12345 & version = Version.2.3.0
    api = ApiRepoMongo("mongodb://localhost:12345/easy-lotto")

  } // add your own port & version parameters in mongoStart method if you need it

  after {
    api.disconnect()
    mongoStop(mongoProps)
  }


  test("should create and retrieve Bets with embedded Mongo") {

    val bets = Bets(
      owner = "edu.chaos@gmail.com",
      lottery = MegaSena,
      from = 1,
      to = 1000,
      numbers = Seq(
        List(1, 2, 3, 4, 5, 6),
        List(1, 2, 3, 4, 5, 8)
      ),
      fellows = Seq.empty
    )

    api.db("bets").size should be(0)

    api.save(bets)

    api.db("bets").size should be(1)

    val betsFind = api.betsFor("edu.chaos@gmail.com", MegaSena)

    betsFind.size should be(1)

    inside(betsFind(0)) { case Bets(owner, lottery, from, to, numbers, fellows, _) =>
      owner should be("edu.chaos@gmail.com")
      lottery should be(MegaSena)
      from should be(1)
      to should be(1000)
      numbers should contain allOf(
        List(1, 2, 3, 4, 5, 6),
        List(1, 2, 3, 4, 5, 8)
        )
      fellows shouldBe empty
    }
  }

  test("should create and retrieve Results with embedded Mongo") {

    val result = Result(
      draw = 1,
      drawDate = "1/1/1111",
      numbers = List(1, 2, 3, 4, 5, 6),
      prizes = List(
        (5, "$ 5,00"),
        (6, "$ 6,00")
      ),
      lottery = MegaSena
    )

    api.db("results").size should be(0)

    api.save(result)

    api.db("results").size should be(1)

    val resultsFind = api.results(MegaSena)

    resultsFind.size should be(1)

    inside(resultsFind(0)) { case Result(draw, drawDate, numbers, prizes, lottery) =>
      lottery should be(MegaSena)
      draw should be(1)
      drawDate should be("1/1/1111")
      numbers should contain allOf(1, 2, 3, 4, 5, 6)
    }
  }

}
