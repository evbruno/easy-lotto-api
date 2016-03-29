package loto

import java.io.InputStream

import akka.http.scaladsl.model.{Uri, ResponseEntity}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.util.ByteString

/**
  * Created by duh on 3/15/16.
  */
object Streammmm extends App {

	import akka.actor.ActorSystem
	import akka.http.scaladsl.Http
	import akka.http.scaladsl.Http.OutgoingConnection
	import akka.http.scaladsl.model.{HttpResponse, HttpRequest}
	import akka.stream.ActorMaterializer
	import akka.stream.scaladsl._

	import scala.concurrent.{Await, Future}

	implicit val system = ActorSystem("QuickStart")
	implicit val materializer = ActorMaterializer()

	implicit val ex = system.dispatcher


	val flow: Flow[HttpRequest, HttpResponse, Future[OutgoingConnection]] = Http().outgoingConnection("www1.caixa.gov.br")

	private val uri: Uri = "loterias/_arquivos/loterias/D_lotfac.zip"

	val source: Future[HttpResponse] =
		Source.single(HttpRequest(uri = uri))
		.via(flow)
		.runWith(Sink.head)

	val fut = source.flatMap { response =>
		println(response)
		println("> STATUS: " + response.status)
		val entity: ResponseEntity = response.entity
		println("> ENTITY: " + entity)
		println("> ENTITY: " + entity.contentType)
		println("> ENTITY: " + entity.contentLengthOption)
//		println("> ENTITY: " + entity.transformDataBytes())
//		val db: Source[ByteString, Any] = entity.dataBytes;

//		val is = Unmarshal(entity.dataBytes).to[InputStream]
//		println("> IS: " + is.si)

		Future.successful(response.status)
	}

	import scala.concurrent.duration._

	val response = Await.result(fut, 1.minute)

	println(response)

	system.shutdown()

}
