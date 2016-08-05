package models

import play.api.data.Form
import play.api.data.Forms._

/**
  * Created by user on 8/5/16.
  */
case class CategoryFormData (name: String, description: String)

object CategoryForm {
  val form = Form(
    mapping(
      "name" -> nonEmptyText,
      "description" -> text
    )(CategoryFormData.apply)(CategoryFormData.unapply)
  )
}
