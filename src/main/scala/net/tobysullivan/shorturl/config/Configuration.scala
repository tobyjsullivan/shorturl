package net.tobysullivan.shorturl.config

import net.tobysullivan.shorturl.InMemoryDataStore

object Configuration {
  val HASH_STORE = InMemoryDataStore
  val STATS_STORE = InMemoryDataStore
}