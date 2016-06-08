package lotto.jobs


trait FileDownloader {

	def download : String

}

class LocalFileDownloader(fileName: String) extends FileDownloader {

	import java.io.File
	require(new File(fileName).exists)

	override def download: String = fileName

}
