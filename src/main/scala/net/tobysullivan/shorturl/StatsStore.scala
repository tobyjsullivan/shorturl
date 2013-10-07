package net.tobysullivan.shorturl

object StatsStore {
  def incrementHashLookupCount(hash: Int)(implicit statsStore: StatsStore) {
    statsStore.incrementHashLookupCount(hash)
  }
  
  def getHashLookupCount(hash: Int)(implicit statsStore: StatsStore): Int = {
    statsStore.getHashLookupCount(hash)
  }
}

trait StatsStore {
  def incrementHashLookupCount(hash: Int)
  
  def getHashLookupCount(hash: Int): Int
}