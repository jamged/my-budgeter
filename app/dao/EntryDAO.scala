package dao

import java.sql.Timestamp
import java.util.Calendar
import javax.inject.Inject
import models.{EntryFormData, Entry, Category}
import play.api.db.slick.{HasDatabaseConfigProvider, DatabaseConfigProvider}
import slick.driver.JdbcProfile
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits.defaultContext

/**
  * Created by user on 8/1/16.
  */
class EntryDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider, categoryDAO: CategoryDAO) extends HasDatabaseConfigProvider[JdbcProfile] {
  import driver.api._

  private val Entries = TableQuery[EntryTable]

  def add(entry: Entry) = {
    println("Adding new entry:" + entry)
    db.run(Entries += entry).map {_ => ()}
  }

  def update(id: Long, data:Entry) = {
    println("Updating entry id: " + id)
    val q = for {e <- Entries if e.id === id } yield (e.amount, e.description, e.catId)
    db.run(q.update(data.amount, data.description, data.catId))
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

  def search(catId: Option[Long] = None, startDay: Timestamp = new Timestamp(0), endDay: Timestamp = new Timestamp(Calendar.getInstance().getTimeInMillis), searchText: String = ""): Future[Seq[(Entry, Category)]] = {
    println("searching with catID: " + catId.toString + " and searchText: " + searchText)
    val q = for {
      e <- Entries if (e.entry_time > startDay) &&
                      (e.entry_time < endDay) &&
                      (e.description.toLowerCase like "%"+searchText.toLowerCase+"%" ) &&
                      (catId match {
                        case Some(cat) => e.catId === cat
                        case None => true
                      })
      c <- e.category
    } yield (e, c)

    db.run(q.sortBy(_._1.entry_time.desc).result)
  }

  private class EntryTable(tag: Tag) extends Table[Entry](tag, "entries") {
    def id = column[Long]("id", O.PrimaryKey,O.AutoInc)
    def amount = column[Double]("amount")
    def description = column[String]("description")
    def entry_time = column[Timestamp]("entry_time")
    def catId = column[Long]("cat_id", O.Default(0))

    def category = foreignKey("cat_fk", catId, categoryDAO.Categories)(_.id, onDelete = ForeignKeyAction.SetNull)

    override def * =
      (id, amount, description, entry_time, catId) <> (Entry.tupled, Entry.unapply)
  }
}
