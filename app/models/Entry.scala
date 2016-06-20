package models

import java.sql.Timestamp
import java.util.Calendar

import play.api.Play
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json._
import play.api.libs.functional.syntax._
import scala.concurrent.Future
import slick.driver.JdbcProfile
import slick.driver.PostgresDriver.api._


// Case class for items stored in "entry" table
case class Entry (id: Long, amount: Double, description: String, entry_time: Timestamp)

object Entry {
  // JSON formatter for reads/writes
  implicit val entryFormat: Format[Entry] = (
    (JsPath \ "id").format[Long] and
    (JsPath \ "amount").format[Double] and
    (JsPath \ "description").format[String] and
    (JsPath \ "entry_time").format[Long]
    )((id:Long, amt:Double, desc:String, time:Long)=> new Entry(id,amt,desc,new Timestamp(time)),
      (e:Entry) => (e.id, e.amount, e.description, e.entry_time.getTime))

  def tupled = (Entry.apply _).tupled

}

object EntryMaker {
  def apply(formData: EntryFormData) = {
    val theTime = Calendar.getInstance().getTimeInMillis
    val credit = formData.transaction.toDouble
    new Entry(0, credit*formData.amount, formData.description, new Timestamp(theTime))
  }
}


// Slick table definition for "entry" table
class EntryTableDef(tag: Tag) extends Table[Entry](tag, "entry") {
  def id = column[Long]("id", O.PrimaryKey,O.AutoInc)
  def amount = column[Double]("amount")
  def description = column[String]("description")
  def entry_time = column[Timestamp]("entry_time")

  override def * =
    (id, amount, description, entry_time) <> (Entry.tupled, Entry.unapply)
}


// Object for "entry" table transactions
object Entries {
  val dbConfig = DatabaseConfigProvider.get[JdbcProfile](Play.current)
  val entries = TableQuery[EntryTableDef]

  def add(entry: Entry) = {
    println("Adding new entry:" + entry)
    dbConfig.db.run(entries += entry)
  }

  def listAll: Future[Seq[Entry]] = {
    dbConfig.db.run(entries.sortBy(_.entry_time.desc).result)
  }
}