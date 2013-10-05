package net.tobysullivan.shorturl

import scala.collection.mutable.HashMap
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import akka.agent.Agent
import scala.concurrent.Future
import scala.concurrent._


object ShortUrl {
  // The available characters for producing a hash. Per the spec, we use a base 62 set
  val CHAR_MAP = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
    
  // Internally, we store and process hashes as ints for better efficiency
  val hashToUrlMap = HashMap[Int, String]()
  val lookupRecord = HashMap[Int, Int]()
  
  def hashUrl(url: String): String = {
    // Check for an existing hash to avoid putting in duplicates.
    val possibleExisting = Await.result(reverseLookupUrlInMap(url), 2 seconds);
    var hashAsInt = 0;
    if(possibleExisting.isDefined) {
      hashAsInt = possibleExisting.get
    } else {
	  hashAsInt = getNextCursor()
	  addUrlHashPairToMap(hashAsInt, url)
    }
    
	val hash = hashFromInt(hashAsInt)
	hash
  }

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

  def statsFor(hash: String): Map[String, Any] = {
    // TODO
    Map[String, Any]()
  }
  
  private val cursor = Agent(0)
  private def getNextCursor(): Int = {
    this.synchronized {
	    cursor send (_ + 1)
	    Await.result(cursor.future, 1 second)
    }
  }
  
  private val hashToUrlMapAgent = Agent(hashToUrlMap)
  private def addUrlHashPairToMap(hash: Int, url: String) {
    hashToUrlMapAgent send (_ += Tuple2(hash, url))
  }
  
  private def findUrlByHashInMap(hash: Int): Option[String] = {
    val map = Await.result(hashToUrlMapAgent.future, 1 second)
    map.get(hash)
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
  
  private def intFromHash(hash: String): Int = {
    val mapSize = this.CHAR_MAP.size
    var out = 0
    
    for(curChar <- hash.toCharArray()) {
      out *= mapSize
      out += this.CHAR_MAP.indexOf(curChar)
    }
    
    out
  }
  
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
  
  private val recordLookupAgent = Agent(lookupRecord)
  private def recordHashLookup(hash: Int) {
    recordLookupAgent sendOff (_ transform { (curHash, count) => if(curHash == hash) { count + 1 } else { count } })
  }
}