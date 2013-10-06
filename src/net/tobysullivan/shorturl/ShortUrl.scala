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
   * Supported keys for statistics collections returned by statsFor(hash: String)
   */
  val STATS_CLICKS = "clicks"
  
  /**
   * This function takes a URL as a string, produces a hash unique to that URL and returns it.
   * If a duplicate URL is provided, the same hash should be returned.
   */
  def hashUrl(url: String): String = {
    // Check for an existing hash to avoid putting in duplicates.
    val possibleExisting = Await.result(reverseLookupUrlInMap(url), 2 seconds);
    var hashAsInt = 0;
    if(possibleExisting.isDefined) {
      // If a hash for this url exists, return that
      hashAsInt = possibleExisting.get
    } else {
      // If this url has not been hashed yet, create a new hash
	  hashAsInt = getNextAvailableHash()
	  addUrlHashPairToMap(hashAsInt, url)
    }
    
	val hash = hashFromInt(hashAsInt)
	hash
  }
  
  /**
   * This method returns the next available hash as an Int in a thread-safe manner.
   */
  private val cursor = Agent(0)
  private def getNextAvailableHash(): Int = {
    val future = cursor alter (_ + 1)
    Await.result(future, 1 second)
  }
  
  private val hashToUrlMapAgent = Agent(HashMap[Int, String]())
  private def addUrlHashPairToMap(hash: Int, url: String) {
    hashToUrlMapAgent send (_ + Tuple2(hash, url))
  }
  
  private def reverseLookupUrlInMap(url: String): Future[Option[Int]] = {
    future {
      val map = Await.result(hashToUrlMapAgent.future, 2 seconds)
      val hit = map.find(kv => { 
        (kv._2 == url)
      })
      
      if(hit.isDefined) {
        Some(hit.get._1)
      } else {
        None
      }
      
    }
  }

  /**
   * This function returns a URL for the given hash. If the supplied hash does not exist
   * in our records, a BadHashException is thrown. 
   */
  def urlFromHash(hash: String): String = {
    val hashAsInt = intFromHash(hash)
    
    val possibleUrl = findUrlByHashInMap(hashAsInt)
    if(possibleUrl.isEmpty) {
      throw new BadHashException("The hash does not exist in the index")
    }
    
    // Record hit for metrics
    recordHashLookup(hashAsInt)
    
    possibleUrl.get
  }
  
  private def findUrlByHashInMap(hash: Int): Option[String] = {
    val map = Await.result(hashToUrlMapAgent.future, 1 second)
    map.get(hash)
  }
  
  /**
   * We use this implicit conversion to help us increment record lookups 
   */ 
  implicit def LookupRecordMapWrapper(m: HashMap[Int, Int]) = new HashMap[Int, Int] {
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
  
  private val recordLookupAgent = Agent(HashMap[Int, Int]())
  private def recordHashLookup(hash: Int) {
    recordLookupAgent send (_ increment hash)
  }

  /**
   * This function returns a collection of statistics for the given hash.
   * Supported keys are supplied by this class as STATS_*
   * Example: ShortUrl.statsFor(someHash).get(ShortUrl.STATS_CLICKS)
   */
  def statsFor(hash: String): Map[String, Any] = {
    val hashAsInt = intFromHash(hash)
    
    Map[String, Any](STATS_CLICKS -> getClicksForHash(hashAsInt))
  }
  
  private def getClicksForHash(hash: Int): Int = {
    val lookupRecord = Await.result(recordLookupAgent.future, 2 seconds)
    
    val count = lookupRecord.get(hash)
    
    if(count.isEmpty) {
      println("count for " + hashFromInt(hash) +  " not defined")
      0
    } else {
      count.get
    }
  }
  
  /**
   * All hashes are represented internally as integers. This function helps
   * us convert from a user-friendly base 62 hash to an Integer
   */
  private def intFromHash(hash: String): Int = {
    val mapSize = this.CHAR_MAP.size
    var out = 0
    
    for(curChar <- hash.toCharArray()) {
      out *= mapSize
      out += this.CHAR_MAP.indexOf(curChar)
    }
    
    out
  }
  
  
  /**
   * All hashes are represented internally as integers. This function helps
   * us convert from a an Integer hash to a user-friendly base 62 hash
   */
  private def hashFromInt(in: Int): String = {
    val mapSize = this.CHAR_MAP.size
    var i = in
    var out = ""
    
    while(i > 0) {
      out += this.CHAR_MAP.charAt(i % mapSize)
      i /= mapSize
    }
    
    // We could reverse the hash in either this function or the intFromHash(String) function.
    // We choose to do it here to offer a decent distribution of hashes. E.i., they will be 
    // produced in the order {..., aa, ba, ca, da, ...}.
    // This could help if we opt to shard the DB.
    out.reverse
  }
  
  
}