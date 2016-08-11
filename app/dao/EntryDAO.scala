package dao

import java.sql.Timestamp
import javax.inject.Inject
import models.{EntryFormData, Entry, Category}
import play.api.db.slick.{HasDatabaseConfigProvider, DatabaseConfigProvider}
import play.db.NamedDatabase
import slick.driver.JdbcProfile
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits.defaultContext

/**
  * Created by user on 8/1/16.
  */
class EntryDAO @Inject()(@NamedDatabase("heroku") protected val dbConfigProvider: DatabaseConfigProvider, categoryDAO: CategoryDAO) extends HasDatabaseConfigProvider[JdbcProfile] {
  import driver.api._

  private val Entries = TableQuery[EntryTable]

  def add(entry: Entry) = {
    println("Adding new entry:" + entry)
    db.run(Entries += entry).map {_ => ()}
  }

  def update(id: Long, data:Entry) = {
    println("Updating entry id: " + id)
    val q = for {e <- Entries if e.id === id } yield (e.amount, e.description, e.catID)
    db.run(q.update(data.amount, data.description, data.catID))
  }

  def delete(id: Long) = {
    println("Deleting entry id: " + id)
    val q = for {e <- Entries if e.id === id } yield e
    db.run(q.delete)
  }

  def listAll: Future[Seq[Entry]] = {
    db.run(Entries.sortBy(_.entry_time.desc).result)
  }

  def listWithCat: Future[Seq[(Entry, Category)]] = {
    val q = for {
      e <- Entries
      c <- e.category
    } yield (e, c)
    db.run(q.sortBy(_._1.entry_time.desc).result)
  }

  def findById(entryId: Long): Future[Option[Entry]] = {
    db.run(Entries.filter(_.id === entryId).result.headOption)
  }

  private class EntryTable(tag: Tag) extends Table[Entry](tag, "entries") {
    def id = column[Long]("id", O.PrimaryKey,O.AutoInc)
    def amount = column[Double]("amount")
    def description = column[String]("description")
    def entry_time = column[Timestamp]("entry_time")
    def catID = column[Long]("cat_id", O.Default(0))

    def category = foreignKey("cat_fk", catID, categoryDAO.Categories)(_.id, onDelete = ForeignKeyAction.SetNull)

    override def * =
      (id, amount, description, entry_time, catID) <> (Entry.tupled, Entry.unapply)
  }
}
