package loto

import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.headers.{`Access-Control-Allow-Credentials`, `Access-Control-Allow-Headers`, `Access-Control-Allow-Methods`, `Access-Control-Allow-Origin`}
import akka.http.scaladsl.server.directives.RespondWithDirectives

trait EnableCORSDirectives extends RespondWithDirectives {

	private val allowedCorsVerbs = List(
		CONNECT, DELETE, GET, HEAD, OPTIONS,
		PATCH, POST, PUT, TRACE
	)

	private val allowedCorsHeaders = List(
		"X-Requested-With", "content-type", "origin", "accept"
	)

	lazy val enableCORS =
		respondWithHeader(`Access-Control-Allow-Origin`.`*`) &
		respondWithHeader(`Access-Control-Allow-Methods`(allowedCorsVerbs)) &
		respondWithHeader(`Access-Control-Allow-Headers`(allowedCorsHeaders)) &
		respondWithHeader(`Access-Control-Allow-Credentials`(true))

}

// http://enable-cors.org/
// Request:

// $ curl -v http://localhost:9000/YOUR_API
// *   Trying 127.0.0.1...
// * Connected to lvh.me (127.0.0.1) port 9000 (#0)
// > GET /YOUR_API HTTP/1.1
// > Host: localhost:9000
// > User-Agent: curl/7.43.0
// > Accept: */*
// >

// Response:

// < HTTP/1.1 200 OK
// < Access-Control-Allow-Origin: *
// < Access-Control-Allow-Methods: CONNECT, DELETE, GET, HEAD, OPTIONS, PATCH, POST, PUT, TRACE
// < Access-Control-Allow-Headers: X-Requested-With, content-type, origin, accept
// < Access-Control-Allow-Credentials: true
// < Server: akka-http/2.3.14
// < Date: Wed, 16 Dec 2015 16:46:19 GMT
// < Content-Type: application/json
// < Content-Length: 384