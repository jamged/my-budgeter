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

  def edit(entryId: Long) = Action.async {
    for {
      entries <- entryDAO.listAll
    } yield {
      entries.find(e=> e.id == entryId) match {
        case Some(entry) =>
          val form = EntryForm.form.fill(EntryFormData(entry.amount.abs, entry.description, if (entry.amount > 0) "1" else "-1"))
          Ok(views.html.create(form, entryId))
        case None =>
          Redirect(routes.Application.index())
      }
    }
  }

  def updateEntry(entryId: Long) = Action { implicit request =>
    println("We're actually updating now!!!")
    EntryForm.form.bindFromRequest.fold(
      errorForm => BadRequest(views.html.create(errorForm, entryId)), // Handle error in form submission
      formData => {
        println("form success!")
        println("value: " + formData.amount)
        println("description: " + formData.description)
        println("transaction: " + formData.transaction)
        entryDAO.update(entryId, EntryMaker(formData))
        Redirect(routes.Application.index())
      }
    )
  }

  def deleteEntry(entryId: Long) = Action {
    println("Deleting a thing")
    entryDAO.delete(entryId)
    Redirect(routes.Application.index())
  }
}
