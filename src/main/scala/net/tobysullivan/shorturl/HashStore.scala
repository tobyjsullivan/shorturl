package net.tobysullivan.shorturl

object HashStore {
  def addHashUrlPair(hash: Int, url: String)(implicit hashStore: HashStore) {
    hashStore.addHashUrlPair(hash, url)
  }

  def findUrl(hash: Int)(implicit hashStore: HashStore): Option[String] = {
    hashStore.findUrl(hash)
  }

  def findHash(url: String)(implicit hashStore: HashStore): Option[Int] = {
    hashStore.findHash(url)
  }

  def findNextAvailableHash()(implicit hashStore: HashStore): Int = {
    hashStore.findNextAvailableHash
  }
}

trait HashStore {
  def addHashUrlPair(hash: Int, url: String)

  def findUrl(hash: Int): Option[String]

  def findHash(url: String): Option[Int]

  def findNextAvailableHash(): Int
}