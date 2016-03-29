import scala.util.{Success, Failure, Try}

def tos(i: Int) : Try[String] = {
	if (i < -1) Failure(new RuntimeException("menor q 0"))
	else Success(i.toString)
}

def tof(i: String) : Try[Float] = {
	if (i == "0") Failure(new RuntimeException("vazio"))
	else Success(i.toFloat)
}

def toq(i: Float) : Try[Float] = {
	if (i <= 0) Failure(new RuntimeException("vazio2"))
	else Success(i * i)
}

val i = tos(-1).map(tof).map(x => toq(x.get))
i.toString
tos(-2).map(tof).map(x => toq(x.get))
tos(0).map(tof).map(x => toq(x.get))

tos(0).map(tof)
tos(-1).map(tof)

tos(10) flatMap tof
tos(0) flatMap(tof)
tos(-1) flatMap(tof)


tos(10) flatMap tof flatMap toq
tos(-1) flatMap tof flatMap toq