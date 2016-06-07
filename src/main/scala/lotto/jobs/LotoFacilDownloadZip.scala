package lotto.jobs

import com.typesafe.config.ConfigFactory

import scala.language.postfixOps

class LotofacilDownloadZip extends ZipFileDownloader {

	override val configKey = "lotofacil"

	override val config = ConfigFactory.load()

}
