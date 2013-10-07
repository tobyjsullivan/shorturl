package net.tobysullivan.shorturl

trait HashStore {
  def addHashUrlPair(hash: Int, url: String)

  def findUrl(hash: Int): Option[String]

  def findHash(url: String): Option[Int]

  def findNextAvailableHash(): Int
}