package dao
import models.{CategoryFormData, Category}
import javax.inject.Inject

import play.api.db.slick.{HasDatabaseConfigProvider, DatabaseConfigProvider}
import play.db.NamedDatabase
import slick.driver.JdbcProfile

import scala.concurrent.Future

/**
  * Created by user on 8/5/16.
  */
class CategoryDAO @Inject()(@NamedDatabase("heroku")protected val dbConfigProvider: DatabaseConfigProvider) extends HasDatabaseConfigProvider[JdbcProfile] {
  import driver.api._

  private[dao] val Categories = TableQuery[CategoryTable]

  def listAll: Future[Seq[Category]] = {
    db.run(Categories.sortBy(_.name.toLowerCase).result)
  }

  def add(category: Category) = {
    db.run(Categories += category)
  }

  def update(id: Long, data:CategoryFormData) = {
    println("Updating category id: " + id)
    val q = for {c <- Categories if c.id === id } yield (c.name, c.description)
    db.run(q.update(data.name, data.description))
  }

  def delete(id: Long) = {
    println("Deleting category id: " + id)
    val q = for {c <- Categories if c.id === id } yield c
    if (id != 0) db.run(q.delete)  // don't delete the default catch-all category
  }

  private[dao] class CategoryTable(tag:Tag) extends Table[Category](tag, "categories"){
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name")
    def description = column[String]("description")

    override def * = (id, name, description) <> (Category.tupled, Category.unapply)
  }

}
