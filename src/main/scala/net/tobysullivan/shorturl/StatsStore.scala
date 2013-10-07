package net.tobysullivan.shorturl

trait StatsStore {
  def incrementHashLookupCount(hash: Int)
  
  def getHashLookupCount(hash: Int): Int
}