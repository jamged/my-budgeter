package models

import play.api.data.Form
import play.api.data.Forms._


case class SearchFormData (catId: Option[Long], startDate: String, endDate: String, searchText: String)

object SearchForm {
  val form = Form(
    mapping(
      "catId" -> optional(longNumber),
      "startDate" -> text,
      "endDate" -> text,
      "searchText" -> text
    )(SearchFormData.apply)(SearchFormData.unapply)
  )
}
