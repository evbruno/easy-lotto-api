package lotto.web

import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.ContentTypes._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import lotto.api.Lottery.MegaSena
import lotto.api.{ApiRepo, Result}
import org.scalatest.{FunSuite, Matchers}

class ApiRouteSuite extends FunSuite
  with Matchers
  with ScalatestRouteTest
  with ApiRoute {

  implicit val apiRepo: ApiRepo = new ApiRepoMem()
  implicit val logger: LoggingAdapter = Logging(system, getClass)

  import lotto.api._

  test("static file route returns correct file") {
    Get("/") ~> staticFilesRoute ~> check {
      status shouldBe OK
      contentType shouldBe `text/html(UTF-8)`
      responseAs[String] should include("<title>Easy Lotto API</title>")
    }
  }

  test("static file route returns `NotFound`") {
    Get("/not-exists-here") ~> Route.seal(staticFilesRoute) ~> check {
      status shouldBe NotFound
    }
  }

  test("results for MegaSena") {
    Get("/api/megasena") ~> apiRoute ~> check {
      status shouldBe OK
      contentType shouldBe `application/json`

      responseAs[String] should include(""""prizes": [[5, "$ 5,00"], [6, "$ 6,00"]]""")
      responseAs[String] should include(""""prizes": [[5, "$ 8,00"], [6, "$ 9,00"]]""")
      responseAs[String] should include(""""numbers": [1, 2, 3, 4, 5, 6]""")
      responseAs[String] should include(""""numbers": [1, 2, 3, 4, 5, 7]""")
      responseAs[String] should include(""""draw": 1""")
      responseAs[String] should include(""""draw": 2""")

      responseAs[List[Result]] should have size (2)
    }
  }

  test("result for MegaSena game 2") {
    Get("/api/megasena/games/2") ~> apiRoute ~> check {
      status shouldBe OK
      contentType shouldBe `application/json`

      val expectedResult = Result(2, "2/2/2222", List(1, 2, 3, 4, 5, 7), List((5, "$ 8,00"), (6, "$ 9,00")), MegaSena)

      responseAs[Result] should be(expectedResult)
    }
  }

  test("api lotteries") {
    Get("/api/lotteries") ~> apiRoute ~> check {
      status shouldBe OK
      contentType shouldBe `application/json`

      val lotteries = responseAs[List[LotteryStatus]]
      lotteries should have size (3)
    }
  }

}


