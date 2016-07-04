package lotto.jobs

import lotto.api.Lottery._

abstract sealed class JobResult {
  val lottery: Lottery
}

case class UpToDate(lottery: Lottery) extends JobResult

case class FewInserts(lottery: Lottery, draws: Seq[Int]) extends JobResult

case class FullImport(lottery: Lottery, total: Int) extends JobResult


