package net.tobysullivan.shorturl.test

import net.tobysullivan.shorturl._

object Configuration {
  val MYSQL_HOSTNAME: String = "192.168.33.10"
  val MYSQL_USERNAME: String = "root"
  val MYSQL_PASSWORD: String = "root"
  val MYSQL_DATABASE: String = "shorturl"
  
// You can optionally use the following InMemory storage instead of the MySQL DB defined below. This will be faster but not persistant
//  val HASH_STORE: HashStore = InMemoryDataStore
//  val STATS_STORE: StatsStore = InMemoryDataStore
   
  val mysqlDb = new MySqlDataStore(MYSQL_HOSTNAME, MYSQL_USERNAME, MYSQL_PASSWORD, MYSQL_DATABASE)
  val HASH_STORE: HashStore = mysqlDb
  val STATS_STORE: StatsStore = mysqlDb
}