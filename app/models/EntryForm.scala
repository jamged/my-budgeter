package models

import play.api.data.Form
import play.api.data.Forms._
import play.api.data.format.Formats._

case class EntryFormData(amount: Double, description: String, transaction: String)

object EntryForm {
  val form = Form(
    mapping(
      "amount" -> of(doubleFormat).verifying("Amount must be positive!", d => d>0),
      "description" -> nonEmptyText,
      "transaction" -> nonEmptyText
    )(EntryFormData.apply)(EntryFormData.unapply)
  )
}
