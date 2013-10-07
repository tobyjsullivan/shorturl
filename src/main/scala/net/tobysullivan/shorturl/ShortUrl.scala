package net.tobysullivan.shorturl

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import akka.agent.Agent
import scala.concurrent.Future
import scala.concurrent._
import scala.collection.immutable.HashMap

object ShortUrl {
  // The available characters for producing a hash. Per the spec, we use a base 62 set
  val CHAR_MAP = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
    
  /**
   * Supported keys for statistics collections returned by ShortUrl.statsFor(hash: String): Map[String, Any]
   */
  val STATS_CLICKS = "clicks"
}

class ShortUrl(hashStore: HashStore, statsStore: StatsStore) {
  
  /**
   * The available hash manager provides an in-memory mechanism for finding what the next
   * available hash is. It only reads from the database once to initialize.
   */
  private def hashManager = new AvailableHashManager(hashStore)
  
  /**
   * This function takes a URL as a string, produces a hash unique to that URL and returns it.
   * If a duplicate URL is provided, the same hash should be returned.
   */
  def hashUrl(url: String): String = {
    // Check for an existing hash to avoid putting in duplicates.
    val possibleExisting = Await.result(reverseLookupUrlInMap(url), 30 seconds);
    var hashAsInt = 0;
    if(possibleExisting.isDefined) {
      // If a hash for this url exists, return that
      hashAsInt = possibleExisting.get
    } else {
      // If this url has not been hashed yet, create a new hash
	  hashAsInt = hashManager.getNext()
	  hashStore.addHashUrlPair(hashAsInt, url)
    }
    
	val hash = hashFromInt(hashAsInt)
	hash
  }
  
  private def reverseLookupUrlInMap(url: String): Future[Option[Int]] = {
    future {
      hashStore.findHash(url)
    }
  }

  /**
   * This function returns a URL for the given hash. If the supplied hash does not exist
   * in our records, a HashNotFoundException is thrown. 
   */
  def urlFromHash(hash: String): String = {
    val hashAsInt = intFromHash(hash)
    
    val possibleUrl = findUrlByHashInMap(hashAsInt)
    if(possibleUrl.isEmpty) {
      throw new HashNotFoundException("The hash does not exist in the index")
    }
    
    // Record hit for metrics
    statsStore.incrementHashLookupCount(hashAsInt)
    
    possibleUrl.get
  }
  
  private def findUrlByHashInMap(hash: Int): Option[String] = {
    hashStore.findUrl(hash)
  }
  
  /**
   * We use this implicit conversion to help us increment record lookups 
   */ 
  implicit private def LookupRecordMapWrapper(m: HashMap[Int, Int]) = new HashMap[Int, Int] {
    def increment(key: Int): HashMap[Int, Int] = {
      if(!m.contains(key)) {
        // This hash has never been looked up before
        m + Tuple2(key, 1)
      } else {
        // Increment an existing count
	    m.map { kv =>
	      if(kv._1 == key) { 
		    (kv._1, kv._2 + 1) 
		  } else { 
		    (kv._1, kv._2)
		  }
	    }
      }
	}
  }

  /**
   * This function returns a collection of statistics for the given hash.
   * Supported keys are supplied by this class as STATS_*
   * Example: ShortUrl.statsFor(someHash).get(ShortUrl.STATS_CLICKS)
   */
  def statsFor(hash: String): Map[String, Any] = {
    val hashAsInt = intFromHash(hash)
    
    Map[String, Any](ShortUrl.STATS_CLICKS -> statsStore.getHashLookupCount(hashAsInt))
  }
  
  /**
   * All hashes are represented internally as integers. This function helps
   * us convert from a user-friendly base 62 hash to an Integer
   */
  private def intFromHash(hash: String): Int = {
    val mapSize = ShortUrl.CHAR_MAP.size
    var out = 0
    
    for(curChar <- hash.reverse.toCharArray()) {
      out *= mapSize
      out += ShortUrl.CHAR_MAP.indexOf(curChar)
    }
    
    out
  }
  
  
  /**
   * All hashes are represented internally as integers. This function helps
   * us convert from a an Integer hash to a user-friendly base 62 hash
   */
  private def hashFromInt(in: Int): String = {
    val mapSize = ShortUrl.CHAR_MAP.size
    var i = in
    var out = ""
    
    while(i > 0) {
      out += ShortUrl.CHAR_MAP.charAt(i % mapSize)
      i /= mapSize
    }
    
    out
  }
}