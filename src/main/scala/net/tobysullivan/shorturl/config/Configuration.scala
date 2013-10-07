package net.tobysullivan.shorturl.config

import net.tobysullivan.shorturl._

object Configuration {
  val MYSQL_HOSTNAME: String = ""
  val MYSQL_USERNAME: String = ""
  val MYSQL_PASSWORD: String = ""
  val MYSQL_DATABASE: String = ""
  
  val HASH_STORE: HashStore = InMemoryDataStore
  // val HASH_STORE: HashStore = new MySqlDataStore(MYSQL_HOSTNAME, MYSQL_USERNAME, MYSQL_PASSWORD, MYSQL_DATABASE)
  
  val STATS_STORE: StatsStore = InMemoryDataStore
}