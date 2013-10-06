package net.tobysullivan.shorturl

import net.tobysullivan.shorturl.config.Configuration

object HashStore {
  private val implementation: HashStore = Configuration.HASH_STORE
  
  def addHashUrlPair(hash: Int, url: String) {
    implementation.addHashUrlPair(hash, url)
  }

  def findUrl(hash: Int): Option[String] = {
    implementation.findUrl(hash)
  }

  def findHash(url: String): Option[Int] = {
    implementation.findHash(url)
  }

  def findNextAvailableHash(): Int = {
    implementation.findNextAvailableHash
  }
}

trait HashStore {
  def addHashUrlPair(hash: Int, url: String)

  def findUrl(hash: Int): Option[String]

  def findHash(url: String): Option[Int]

  def findNextAvailableHash(): Int
}