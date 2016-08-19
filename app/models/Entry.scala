package models

import java.sql.Timestamp
import java.util.Calendar

import play.api.libs.json._
import play.api.libs.functional.syntax._


// Case class for items stored in "entry" table
case class Entry (id: Long = 0, amount: Double, description: String, entry_time: Timestamp, catId: Long = 0)

object Entry {
  // JSON formatter for reads/writes
  implicit val entryFormat: Format[Entry] = (
    (JsPath \ "id").format[Long] and
    (JsPath \ "amount").format[Double] and
    (JsPath \ "description").format[String] and
    (JsPath \ "entry_time").format[Long] and
    (JsPath \ "cat_id").format[Long]
    )((id:Long, amt:Double, desc:String, time:Long, catId:Long)=> new Entry(id,amt,desc,new Timestamp(time), catId),
      (e:Entry) => (e.id, e.amount, e.description, e.entry_time.getTime, e.catId))

  def tupled = (Entry.apply _).tupled

}

object EntryMaker {
  def apply(formData: EntryFormData) = {
    val theTime = Calendar.getInstance().getTimeInMillis
    val credit = formData.transaction.toDouble
    new Entry(0, credit*formData.amount, formData.description, new Timestamp(theTime), formData.catId)
  }
}
