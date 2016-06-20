package controllers

import play.api._
import play.api.mvc._
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.libs.json.Json

import models._

class Application extends Controller {

  def index = Action.async { implicit request =>
    Entries.listAll map {entries =>
      Ok(views.html.index(entries))
    }
  }

  def getEntryInfo = Action {
    Ok(views.html.create(EntryForm.form))
  }

  def addEntry() = Action { implicit request =>
    EntryForm.form.bindFromRequest.fold(
      errorForm => BadRequest(views.html.create(errorForm)), // Handle error in form submission
      formData => {
        println("form success!")
        println("value: " + formData.amount)
        println("description: " + formData.description)
        println("transaction: " + formData.transaction)
        Entries.add(EntryMaker(formData))
        Redirect(routes.Application.index())
      }
    )
  }

  def toJson = Action.async {
    Entries.listAll map {entries =>
      Ok(Json.toJson(entries))
    }
  }
}
