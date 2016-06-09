package lotto


package object api {

	type Numbers = IndexedSeq[Int]

	type Prize = (Int, String)

	type Email = String

	type Draw = Int

	type _ID = String

	import scala.language.implicitConversions

	implicit def fromListToIndexedSeq(in: List[Int]) : Numbers = in.toIndexedSeq

}
