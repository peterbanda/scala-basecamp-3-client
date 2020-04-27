package com.bnd.basecampclient.model

import org.joda.time.DateTime
import play.api.libs.json.Json
import com.bnd.basecampclient.model.Project.dateFormat

case class Upload(
  id: Long,
  status: String,
  visible_to_clients: Boolean,
  created_at: DateTime,
  updated_at: DateTime,
  title: String,
  inherits_status: Boolean,
  `type`: String,
  url: String,
  app_url: String,
  //  bookmark_url: String, // TODO: Automatic Play JSON formatter supports only ~20 fields so have to comment out a few
  //  subscription_url: String,
  comments_count: Int,
  //  comments_url: String,
  position: Int,
  description: Option[String],
  content_type: String,
  byte_size: Long,
  filename: String,
  download_url: String,
  app_download_url: String,
  parent: Parent,
  bucket: Bucket,
  creator: Creator
)

case class Bucket(
  id: Long,
  name: String,
  `type`: String
)

case class Parent(
  id: Long,
  title: String,
  `type`: String,
  url: String,
  app_url: String
)

case class Creator(
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
  avatar_url: String
)

object Upload {
  implicit val parentFormat = Json.format[Parent]
  implicit val bucketFormat = Json.format[Bucket]
  implicit val creatorFormat = Json.format[Creator]

  implicit val format = Json.format[Upload]
}