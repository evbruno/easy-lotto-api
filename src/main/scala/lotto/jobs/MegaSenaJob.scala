package lotto.jobs

import lotto.api.{MegaSena, ApiRepo}

class MegaSenaJob(implicit val apiRepo : ApiRepo) extends JobTemplate {

	private val ENV_KEY: String = "EASY_LOTTO_API_MEGA_SENA_FILE"

	override val downloader =
		if (sys.env(ENV_KEY) != null) {
			val f = sys.env(ENV_KEY)
			warn(s"Local file for Mega Sena: $f")
			new LocalFileDownloader(f)
		} else
			new MegaSenaDownloadZip()

	override val parserFactory = (file: String) => new MegaSenaHtmlParser(file)

	override val lottery = MegaSena

}
