package dao
import models.Category
import javax.inject.Inject

import play.api.db.slick.{HasDatabaseConfigProvider, DatabaseConfigProvider}
import slick.driver.JdbcProfile

import scala.concurrent.Future

/**
  * Created by user on 8/5/16.
  */
class CategoryDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) extends HasDatabaseConfigProvider[JdbcProfile] {
  import driver.api._

  val Categories = TableQuery[CategoryTable]

  def listAll: Future[Seq[Category]] = {
    db.run(Categories.sortBy(_.name).result)
  }

  def add(category: Category) = {
    db.run(Categories += category)
  }

  class CategoryTable(tag:Tag) extends Table[Category](tag, "categories"){
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name")
    def description = column[String]("description")

    override def * = (id, name, description) <> (Category.tupled, Category.unapply)
  }

}
