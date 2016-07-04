package lotto.jobs

import lotto.api.ApiRepo
import lotto.api.Lottery.Lotofacil

class LotofacilJob(implicit val apiRepo: ApiRepo) extends JobTemplate {

  private val ENV_KEY: String = "EASY_LOTTO_API_LOTOFACIL_FILE"

  override val downloader =
    if (System.getenv(ENV_KEY) != null) {
      val f = sys.env(ENV_KEY)
      warn(s"Local file for Lotofacil: $f")
      new LocalFileDownloader(f)
    } else
      new LotofacilDownloadZip()

  override val parserFactory = (file: String) => new LotofacilHtmlParser(file)

  override val lottery = Lotofacil

}
