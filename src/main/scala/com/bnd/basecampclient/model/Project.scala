package com.bnd.basecampclient.model

import org.joda.time.DateTime
import play.api.libs.json.{Format, Json, Reads, Writes, __}

case class Project(
  id: Long,
  status: String,
  created_at: DateTime,
  updated_at: DateTime,
  name: String,
  description: Option[String],
  purpose: String,
  clients_enabled: Boolean,
  bookmark_url: String,
  url: String,
  app_url: String,
  dock: Seq[Dock]
)

case class Dock(
  id : Long,
  title : String,
  name : String,
  enabled : Boolean,
  position : Option[Int],
  url : String,
  app_url : String
)

object Project {

  private val dateTimeFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSZ"

  implicit val dateFormat = Format[DateTime](
    Reads.jodaDateReads(dateTimeFormat),
    Writes.jodaDateWrites(dateTimeFormat)
  )

  implicit val dockFormat = Json.format[Dock]
  implicit val format = Json.format[Project]
}