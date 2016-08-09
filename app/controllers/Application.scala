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
          Ok(views.html.newEntry(form, categories, entryId))
        case None =>
          Redirect(routes.Application.index())
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
      Ok(views.html.newCategory(CategoryForm.form, categories))
    }
  }

  def addCategory() = Action.async { implicit request =>
      categoryDAO.listAll map { categories =>
        CategoryForm.form.bindFromRequest.fold(
          errorForm => BadRequest(views.html.newCategory(errorForm, categories)), // Handle error in form submission
          formData => {
            categories.exists(c => c.name.equals(formData.name)) match {
              case true =>
                println("Already have category named" + formData.name)
                BadRequest(views.html.newCategory(CategoryForm.form.fill(formData), categories, msg = "Already have a Category named " + formData.name ))
              case false =>
                println ("new category form success!")
                println ("name: " + formData.name)
                println ("description: " + formData.description)
                categoryDAO.add (Category (0, formData.name, formData.description) )
                Redirect (routes.Application.showCategories() )
            }
          }
        )
      }
  }

  def editCategory(catId: Long) = Action.async {
    for {
      categories <- categoryDAO.listAll
    } yield {
      categories.find(c=> c.id == catId && c.id != 0) match {
        case Some(cat) =>
          val form = CategoryForm.form.fill(CategoryFormData(cat.name, cat.description))
          Ok(views.html.newCategory(form, categories, catId))
        case None =>
          Redirect(routes.Application.index())
      }
    }
  }

  def updateCategory(catId: Long) = Action.async { implicit request =>
    categoryDAO.listAll map { categories =>
      CategoryForm.form.bindFromRequest.fold(
        errorForm => BadRequest(views.html.newCategory(errorForm, categories, catId)), // Handle error in form submission
        formData => {
          categories.exists(c => c.name.equals(formData.name) && c.id != catId) match {
            case true =>
              BadRequest(views.html.newCategory(CategoryForm.form.fill(formData), categories, catId, msg = "Already have a Category named " + formData.name ))
            case false =>
              categoryDAO.update(catId, formData)
              Redirect(routes.Application.showCategories())
          }
        }
      )
    }
  }

  def deleteCategory(catId: Long) = Action {
    categoryDAO.delete(catId)
    Redirect(routes.Application.showCategories())
  }

  def showCategories = Action.async {
    categoryDAO.listAll map { categories =>
      Ok(views.html.categories(categories.filter(c=>c.id!=0)))
    }
  }
}
