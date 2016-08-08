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

class Application @Inject() (entryDAO: EntryDAO, categoryDAO: CategoryDAO) extends Controller {

  def index = Action.async { implicit request =>
    entryDAO.listWithCat map {entries =>
      Ok(views.html.index(entries))
    }
  }

  def defineNewEntry = Action.async {
    categoryDAO.listAll map {categories =>
      Ok(views.html.create(EntryForm.form, categories))
    }
  }

  def addEntry() = Action.async { implicit request =>
    categoryDAO.listAll map { categories =>
      EntryForm.form.bindFromRequest.fold(
        errorForm => BadRequest(views.html.create(errorForm, categories)), // Handle error in form submission
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
          val form = EntryForm.form.fill(EntryFormData(entry.amount.abs, entry.description, if (entry.amount > 0) "1" else "-1", entry.catID))
          Ok(views.html.create(form, categories, entryId))
        case None =>
          Redirect(routes.Application.index())
      }
    }
  }

  def updateEntry(entryId: Long) = Action.async { implicit request =>
    categoryDAO.listAll map { categories =>
      EntryForm.form.bindFromRequest.fold(
        errorForm => BadRequest(views.html.create(errorForm, categories, entryId)), // Handle error in form submission
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
  }

  def deleteEntry(entryId: Long) = Action {
    println("Deleting a thing")
    entryDAO.delete(entryId)
    Redirect(routes.Application.index())
  }


  def defineNewCategory = Action.async {
    categoryDAO.listAll map {categories =>
      Ok(views.html.newcategory(CategoryForm.form, categories))
    }
  }

  def addCategory() = Action.async { implicit request =>
      categoryDAO.listAll map { categories =>
        CategoryForm.form.bindFromRequest.fold(
          errorForm => BadRequest(views.html.newcategory(errorForm, categories)), // Handle error in form submission
          formData => {
            println("form success!")
            println("name: " + formData.name)
            println("description: " + formData.description)
            categoryDAO.add(Category(0, formData.name, formData.description))
            Redirect(routes.Application.index())
          }
        )
      }
  }

  def editCategory(catID: Long) = Action {
    printf("should edit category with ID: %d", catID)
    Redirect(routes.Application.index())
  }

  def updateCategory(catID: Long) = Action {
    Redirect(routes.Application.index())
  }

  def showCategories = Action.async {
    categoryDAO.listAll map { categories =>
      Ok(views.html.categories(categories))
    }
  }
}
