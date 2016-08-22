package controllers

import javax.inject.Inject

import dao.CategoryDAO
import models.{CategoryFormData, Category, CategoryForm}
import play.api.mvc.{Action, Controller}

import play.api.Play.current
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.i18n.Messages.Implicits._


class CategoryController @Inject() (categoryDAO: CategoryDAO) extends Controller {

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
              //TODO
              Redirect (routes.CategoryController.showCategories() )
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
          // TODO
          Redirect(routes.CategoryController.showCategories())
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
              //todo
              Redirect(routes.CategoryController.showCategories())
          }
        }
      )
    }
  }

  def deleteCategory(catId: Long) = Action {
    categoryDAO.delete(catId)
    //todo
    Redirect(routes.CategoryController.showCategories())
  }

  def showCategories = Action.async {
    categoryDAO.listAll map { categories =>
      Ok(views.html.categories(categories.filter(c=>c.id!=0)))
    }
  }
}
