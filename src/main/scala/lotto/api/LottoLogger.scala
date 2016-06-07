package lotto.api

import org.slf4j.LoggerFactory
import pl.project13.scala.rainbow.Rainbow

trait LottoLogger {

//	private lazy val logger = LoggerFactory.getLogger(this.getClass)

	private val width = 80
	private val _char = "#"
	private val _line = _char * width

	def line() = println(_line)

	def lineFor(what: String) = {
		val space = " " * (width - what.length - 5)
		s"# $what $space ${_char}"
	}

	def info(what: String) = println(lineFor(what))

	import Rainbow._

	def error(what: String) = Console.err.println(lineFor(what))

	def error(what: String, t: Throwable) = {
		Console.err.println(lineFor(what + ":").red)
		Console.err.println(lineFor(" -> " + t.getMessage).red)
		Console.err.println(lineFor(" -> stack: ").red)
		t.printStackTrace(Console.err)
	}

}
