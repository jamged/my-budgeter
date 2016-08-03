package dao

import java.sql.Timestamp
import javax.inject.Inject
import models.{EntryFormData, Entry}
import play.api.db.slick.{HasDatabaseConfigProvider, DatabaseConfigProvider}
import slick.driver.JdbcProfile
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits.defaultContext

/**
  * Created by user on 8/1/16.
  */
class EntryDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) extends HasDatabaseConfigProvider[JdbcProfile] {
  import driver.api._

  private val Entries = TableQuery[EntryTable]

  def add(entry: Entry) = {
    println("Adding new entry:" + entry)
    db.run(Entries += entry).map {_ => ()}
  }

  def update(id: Long, data:Entry) = {
    println("Updating entry id: " + id)
    val q = for {e <- Entries if e.id === id } yield (e.amount, e.description)
    db.run(q.update(data.amount, data.description))
  }

  def delete(id: Long) = {
    println("Deleting entry id: " + id)
    val q = for {e <- Entries if e.id === id } yield e
    db.run(q.delete)
  }

  def listAll: Future[Seq[Entry]] = {
    db.run(Entries.result)
  }

  def findById(entryId: Long): Future[Option[Entry]] = {
    db.run(Entries.filter(_.id === entryId).result.headOption)
  }

  private class EntryTable(tag: Tag) extends Table[Entry](tag, "entry") {
    def id = column[Long]("id", O.PrimaryKey,O.AutoInc)
    def amount = column[Double]("amount")
    def description = column[String]("description")
    def entry_time = column[Timestamp]("entry_time")

    override def * =
      (id, amount, description, entry_time) <> (Entry.tupled, Entry.unapply)
  }
}
