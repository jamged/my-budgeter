package controllers

import javax.inject.Inject

import dao.{CategoryDAO, EntryDAO}
import models.{SearchForm, EntryFormData, EntryMaker, EntryForm}
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import play.twirl.api.Html
import play.api.Play.current

import play.api.i18n.Messages.Implicits._
import scala.concurrent.ExecutionContext.Implicits.global


class EntryController @Inject() (entryDAO: EntryDAO, categoryDAO: CategoryDAO) extends Controller {

  def showEntries = Action.async { implicit request =>
    entryDAO.listWithCat map {entries =>
      Ok(views.html.index(entries)(Html("hi")))
    }
  }

  def defineNewEntry = Action.async {
    categoryDAO.listAll map {categories =>
      Ok(views.html.newEntry(EntryForm.form, categories))
    }
  }

  def addEntry() = Action.async { implicit request =>
    categoryDAO.listAll map { categories =>
      EntryForm.form.bindFromRequest.fold(
        errorForm => BadRequest(views.html.newEntry(errorForm, categories)), // Handle error in form submission
        formData => {
          println("form success!")
          println("value: " + formData.amount)
          println("description: " + formData.description)
          println("transaction: " + formData.transaction)
          entryDAO.add(EntryMaker(formData))
          //todo
          Redirect(routes.EntryController.showEntries())
        }
      )
    }
  }

  def toJson = Action.async {
    entryDAO.listAll map {entries =>
      Ok(Json.toJson(entries))
    }
  }

  def editEntry(entryId: Long) = Action.async {
    for {
      entries <- entryDAO.listAll
      categories <- categoryDAO.listAll
    } yield {
      entries.find(e=> e.id == entryId) match {
        case Some(entry) =>
          val form = EntryForm.form.fill(EntryFormData(entry.amount.abs, entry.description, if (entry.amount > 0) "1" else "-1", entry.catId))
          Ok(views.html.newEntry(form, categories, entryId))
        case None =>
          //todo
          Redirect(routes.EntryController.showEntries())
      }
    }
  }

  def updateEntry(entryId: Long) = Action.async { implicit request =>
    categoryDAO.listAll map { categories =>
      EntryForm.form.bindFromRequest.fold(
        errorForm => BadRequest(views.html.newEntry(errorForm, categories, entryId)), // Handle error in form submission
        formData => {
          println("new entry form success!")
          println("value: " + formData.amount)
          println("description: " + formData.description)
          println("transaction: " + formData.transaction)
          entryDAO.update(entryId, EntryMaker(formData))
          //todo
          Redirect(routes.EntryController.showEntries())
        }
      )
    }
  }

  def deleteEntry(entryId: Long) = Action {
    println("Deleting a thing")
    entryDAO.delete(entryId)
    //todo
    Redirect(routes.EntryController.showEntries())
  }

  def clearSearch = Action.async {
    for {
      entries <- entryDAO.listWithCat
      allCategories <- categoryDAO.listAll
    } yield {
      entries.foreach(e=>println("entry catID: " + e._1.catId + " with category: " + e._2.name))
      Ok(views.html.search(entries, allCategories, SearchForm.form))
    }
  }

  def searchEntries = Action.async { implicit request =>
    SearchForm.form.bindFromRequest.fold(
      errorForm => {
        println("ERROR IN ENTRY SEARCH FORM???")
        clearSearch.apply(request)
      },
      formData => {
        println("Search form success!")
        println("category ID: " + formData.catId)
        println("startDate: " + formData.startDate)
        println("endDate: " + formData.endDate)
        println("searchText: " + formData.searchText)
        for {
          results <- entryDAO.search(formData.catId, searchText=formData.searchText)
          allCategories <- categoryDAO.listAll
        } yield {
          Ok(views.html.search(results, allCategories, SearchForm.form.fill(formData)))
        }
      }
    )
  }
}
