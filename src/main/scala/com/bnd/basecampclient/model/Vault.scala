package com.bnd.basecampclient.model

import org.joda.time.DateTime
import play.api.libs.json.Json
import com.bnd.basecampclient.model.Project.dateFormat

case class Vault(
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
  bookmark_url: String,
  position: Int,
  documents_count: Int,
  documents_url: String,
  uploads_count: Int,
  uploads_url: String,
  vaults_count: Int,
  vaults_url: String,
  parent: Parent,
  bucket: Bucket,
  creator: Creator
)

object Vault {
  implicit val parentFormat = Json.format[Parent]
  implicit val bucketFormat = Json.format[Bucket]
  implicit val creatorFormat = Json.format[Creator]

  implicit val format = Json.format[Vault]
}