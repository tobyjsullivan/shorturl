package net.tobysullivan.shorturl

object StatsStore {
  val implementation: StatsStore = InMemoryDataStore
  
  def incrementHashLookupCount(hash: Int) {
    implementation.incrementHashLookupCount(hash)
  }
  
  def getHashLookupCount(hash: Int): Int = {
    implementation.getHashLookupCount(hash)
  }
}

trait StatsStore {
  def incrementHashLookupCount(hash: Int)
  
  def getHashLookupCount(hash: Int): Int
}