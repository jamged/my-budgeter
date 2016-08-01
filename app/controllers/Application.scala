package controllers

import javax.inject.Inject

import play.api._
import play.api.mvc._
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.libs.json.Json

import dao._
import models._

class Application @Inject() (entryDAO: EntryDAO) extends Controller {

  def index = Action.async { implicit request =>
    entryDAO.listAll map {entries =>
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
        entryDAO.add(EntryMaker(formData))
        Redirect(routes.Application.index())
      }
    )
  }

  def toJson = Action.async {
    entryDAO.listAll map {entries =>
      Ok(Json.toJson(entries))
    }
  }
}
