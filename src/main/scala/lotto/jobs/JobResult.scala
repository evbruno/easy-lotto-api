package lotto.jobs

sealed class LotteryType

case object Lotofacil extends LotteryType

case object MegaSena extends LotteryType



abstract sealed class JobResult {
	val lottery: LotteryType
}

case class UpToDate(lottery: LotteryType) extends JobResult

case class FewInserts(lottery: LotteryType, draws: Array[Int]) extends JobResult

case class FullImport(lottery: LotteryType, total: Int) extends JobResult


