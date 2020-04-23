package com.bnd.basecampclient.model

import org.joda.time.DateTime
import play.api.libs.json.{Format, Json, Reads, Writes}
import com.bnd.basecampclient.model.Project.dateFormat

case class Person(
  id: Long,
  attachable_sgid: String,
  name: String,
  email_address: String,
  personable_type: String,
  title: Option[String],
  bio: Option[String],
  created_at: DateTime,
  updated_at: DateTime,
  admin: Boolean,
  owner: Boolean,
  client: Boolean,
  time_zone: String,
  avatar_url: String,
  company: Option[Company]
)

case class Company(
  id: Long,
  name: String
)

object Person {
  implicit val companyFormat = Json.format[Company]
  implicit val format = Json.format[Person]
}