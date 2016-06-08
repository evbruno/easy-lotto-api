package lotto.jobs

import java.io.{File, FileOutputStream}
import java.net.{CookieHandler, CookieManager, CookiePolicy, URL}
import java.util.zip.ZipFile

import com.typesafe.config.{Config, ConfigFactory}
import lotto.api.LottoLogger
import org.apache.commons.io.IOUtils

import scala.sys.process._

abstract class ZipFileDownloader extends FileDownloader with LottoLogger {

	val configKey: String
	val config: Config

	CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL))

	private val prefix = "lotteries."

	private def url = config.getString(s"$prefix$configKey.url")

	private def htmlFileName = config.getString(s"$prefix$configKey.html-file-name")

	override def download: String = {
		val outputZip = File.createTempFile(configKey, ".zip")
		val outputHtml = File.createTempFile(configKey, ".html")

		info(s"Downloading from $url")

		import scala.language.postfixOps

		new URL(url) #> outputZip !!

		info(s"Download file $outputZip //  ${outputZip.exists} // ${outputZip.length}")

		val zipFile = new ZipFile(outputZip)
		val in = zipFile.getInputStream(zipFile.getEntry(htmlFileName))

		val out = new FileOutputStream(outputHtml)
		IOUtils.copy(in, out)

		IOUtils.closeQuietly(in)
		IOUtils.closeQuietly(out)

		info(s"HTML output file $outputHtml // ${outputHtml.exists} // ${outputHtml.length}")

		outputHtml.getAbsolutePath
	}

}
