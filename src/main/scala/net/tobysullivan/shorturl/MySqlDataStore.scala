package net.tobysullivan.shorturl

import scala.slick.driver.MySQLDriver.simple._
import Database.threadLocalSession 

import scala.slick.jdbc.meta.MTable
import scala.slick.jdbc.UnitInvoker

object MySqlDataStore {
  val TABLE_NAME = "tbl_urls"
}

case class MySqlDataStore(hostname: String, username: String, password: String, database: String) extends HashStore {

  object Urls extends Table[(Int, String)](MySqlDataStore.TABLE_NAME) {
    def hash = column[Int]("hash", O.PrimaryKey)
    def url = column[String]("url")
    def * = hash ~ url
    def idx_url = index("idx_url", (url), unique = true)
  }

  val db = Database.forURL(url = "jdbc:mysql://" + hostname + ":3306/" + database, user = username, password = password, driver = "com.mysql.jdbc.Driver")

  db.withSession {
    val existing = MTable.getTables(MySqlDataStore.TABLE_NAME)
    if (existing.list().isEmpty) {
      Urls.ddl.create
    }
  }
  
  def addHashUrlPair(hash: Int, url: String) {
    // Check if the supplied hash already exists in the DB
//    val existing = findUrl(hash)
//    if(existing.isDefined) {
//      throw new DuplicateHashException("The specified hash already exists in the database")
//    }
    
    db.withSession {
    	Urls.insert(hash, url)
    }
  }

  def findUrl(hash: Int): Option[String] = {
    db.withSession {
      val query = for {
        u <- Urls if u.hash === hash
      } yield (u.url)
      
      val url = query.firstOption
      
      url
    }
  }

  def findHash(url: String): Option[Int] = {
    db.withSession {
      val query = for {
        u <- Urls if u.url === url
      } yield (u.hash)
      
      val hash = query.firstOption
      
      hash
    }
  }

  def findNextAvailableHash(): Int = {
    db.withSession {
      val max = Query(Urls.map(_.hash).max).first
      
      if(max.isEmpty) {
        1
      } else {
        max.get + 1
      }
    }
  }
}