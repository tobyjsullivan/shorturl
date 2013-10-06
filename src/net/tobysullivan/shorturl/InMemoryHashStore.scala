package net.tobysullivan.shorturl

import scala.collection.mutable.HashMap

class InMemoryHashStore extends HashStore {

  private val hashStore = new HashMap[Int, String]
  def findNextAvailableHash(): Int = {
    // Search the mappings for the largest hash used
    val maxHash = hashStore.foldLeft(0) { (max, pair) =>
      Math.max(max, pair._1)
    }

    (maxHash + 1)
  }

  def addHashUrlPair(hash: Int, url: String) {
    val existing = hashStore.get(hash)
    if (existing.isDefined) {
      throw new DuplicateHashException("The specified hash already exists")
    }

    hashStore += Tuple2(hash, url)
  }

  def findUrl(hash: Int): Option[String] = {
    hashStore.get(hash)
  }

  def findHash(url: String): Option[Int] = {
    val pair = hashStore.find { kv =>
      kv._2 == url
    }
    
    if(pair.isDefined) {
      Some(pair.get._1)
    } else {
      None
    }
  }
}