package lotto.web

import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.model.ContentTypes._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import lotto.api.ApiRepo
import org.scalatest.{FunSuite, Matchers}

class ApiRouteSuite extends FunSuite
	with Matchers
	with ScalatestRouteTest
	with ApiRoute {

	implicit val apiRepo: ApiRepo = new ApiRepoMem()
	implicit val logger: LoggingAdapter = Logging(system, getClass)

	test("static file route returns correct file") {
		Get("/") ~> staticFilesRoute ~> check {
			status shouldBe OK
			contentType shouldBe `text/html(UTF-8)`
			responseAs[String] should include ( "<title>Easy Lotto API</title>" )
		}
	}

	test("static file route returns `NotFound`") {
		Get("/not-exists-here") ~> Route.seal(staticFilesRoute) ~> check {
			status shouldBe NotFound
		}
	}

	// import org.scalactic.Explicitly._
	// import org.scalactic.StringNormalizations._

	test("results for MegaSena") {
		Get("/api/megasena") ~> apiRoute ~> check {
			status shouldBe OK
			contentType shouldBe `application/json`
			println("-----")
			println(responseAs[String])
			println("-----")

			responseAs[String].trim should include (""""prizes": [[5, "$ 5,00"], [6, "$ 6,00"]]""")
		}
	}


}


