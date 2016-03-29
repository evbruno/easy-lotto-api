package lotto.api

trait LottoLogger {

	private val width = 80
	private val _char = "#"
	private val _line = _char * width

	def line() = println(_line)

	def info(what: String): Unit = {
		val space = " " * (width - what.length - 5)
		println(s"# $what $space ${_char}")
	}
	
}
