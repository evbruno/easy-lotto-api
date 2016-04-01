package lotto.api


import agatetepe.Entity.Request._
import agatetepe.Entity.Response
import agatetepe.{HttpClient, _}

import scala.concurrent.{ExecutionContext, _}
import scala.concurrent.duration._
import scala.util.{Failure, Success}

/*
  curl "https://api.postmarkapp.com/email" \
  -X POST \
  -H "Accept: application/json" \
  -H "Content-Type: application/json" \
  -H "X-Postmark-Server-Token: XXXXX" \
  -d "{From: 'XXXX', To: 'XXXX', Subject: 'Postmark test', HtmlBody: '<html><body><strong>Hello</strong> dear Postmark user.</body></html>'}"
 */
object SendEmail extends LottoLogger {

	private lazy val client = HttpClient()

	def send(emailBody: String)(implicit ex: ExecutionContext) {
		info(s"Sending email init....")

		val postMarkToken = sys.env("POSTMARK_API_TOKEN")
		val to = sys.env("EMAIL_TO")
		val from = "bot@bruno.etc.br"
		val subject = "Easy Lotto API"

		val emailBody2 = emailBody.replaceAll("'", "\\'")

		val jsonBody = s"""{From: '$from', To: '$to', Subject: '$subject', HtmlBody: '$emailBody2'}"""

		line
		info(s"Sending email. Json: $jsonBody")
		line

		val url = "https://api.postmarkapp.com/email"
		val request = post(url) json (jsonBody) withHeader("Accept", "application/json") withHeader("X-Postmark-Server-Token", postMarkToken)

		val future: Future[Response] = client.asyncProcess(request)

		future.onComplete {
			case Success(result) => info(s"Email sent result: ${result.body.asString}")
			case Failure(t) => {
				info(s"Email sent result: ${t}")
				t.printStackTrace()
			}
		}

		Await.ready(future, 5.minutes)

		line
		info(s"Email thread is done.")
		line
	}

}
