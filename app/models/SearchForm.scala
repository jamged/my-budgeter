package models

import java.sql.Timestamp

import play.api.data.Form
import play.api.data.Forms._


case class SearchFormData (catId: Option[Long], startDate: String, endDate: String, searchText: String)

object SearchForm {
  val form = Form(
    mapping(
      "catId" -> optional(longNumber),
      "startDate" -> text.verifying("Enter a valid date in YYYY-MM-DD format!", res=> res=="" || validator(res)),
      "endDate" -> text.verifying("Enter a valid date in YYYY-MM-DD format!", res=> res=="" || validator(res)),
      "searchText" -> text
    )(SearchFormData.apply)(SearchFormData.unapply)
  )

  def validator(dateString: String) : Boolean = try {
    Timestamp.valueOf(dateString + " 00:00:00")
    true
  } catch {
    case e: IllegalArgumentException => false
  }
}
