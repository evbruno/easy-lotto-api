package loto

case class UserInfo(email: Email,
					name: String,
				   	tokens: Seq[String] = Seq(),
					pictureUrl: Option[String] = None,
					whiteList : Boolean = false)
