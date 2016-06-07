package lotto.jobs

import com.typesafe.config.ConfigFactory

import scala.language.postfixOps

class MegaSenaDownloadZip extends ZipFileDownloader {

	override val configKey = "mega-sena"

	override val config = ConfigFactory.load()

}

object SpikeApp2 extends App {
	val op = new MegaSenaDownloadZip().download
	println(s"Output path: $op")
}