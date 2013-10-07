package net.tobysullivan.shorturl

import scala.slick.driver.MySQLDriver.simple._
import Database.threadLocalSession 

import scala.slick.jdbc.meta.MTable
import scala.slick.jdbc.UnitInvoker

object MySqlDataStore {
  val TABLE_URLS = "tbl_urls"
  val TABLE_LOOKUPS = "tbl_lookups"
}

case class MySqlDataStore(hostname: String, username: String, password: String, database: String) extends HashStore with StatsStore {

  object Urls extends Table[(Int, String)](MySqlDataStore.TABLE_URLS) {
    def hash = column[Int]("hash", O.PrimaryKey)
    def url = column[String]("url")
    def * = hash ~ url
    def idx_url = index("idx_url", (url), unique = true)
  }
  
  object Lookups extends Table[(Int, Int)](MySqlDataStore.TABLE_LOOKUPS) {
    def hash = column[Int]("hash", O.PrimaryKey)
    def count = column[Int]("count")
    def * = hash ~ count
  }

  val db = Database.forURL(url = "jdbc:mysql://" + hostname + ":3306/" + database, user = username, password = password, driver = "com.mysql.jdbc.Driver")

  db.withSession {
    val existingUrlsTable = MTable.getTables(MySqlDataStore.TABLE_URLS)
    if (existingUrlsTable.list().isEmpty) {
      Urls.ddl.create
    }
    
    val existingLookupsTable = MTable.getTables(MySqlDataStore.TABLE_LOOKUPS)
    if (existingLookupsTable.list().isEmpty) {
      Lookups.ddl.create
    }
  }
  
  def addHashUrlPair(hash: Int, url: String) {
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
   
  def incrementHashLookupCount(hash: Int) {
    db.withSession {
      val q = for { l <- Lookups if l.hash === hash } yield l.count
      
      val current = q.firstOption
      
      if(current.isDefined) {
        q.update(current.get + 1)
      } else {
        Lookups.insert(hash, 1)
      }
      
    }
  }
  
  def getHashLookupCount(hash: Int): Int = {
    db.withSession {
      val query = for {
        l <- Lookups if l.hash === hash
      } yield (l.count)
      
      query.firstOption.getOrElse(0)
    }
  }
}