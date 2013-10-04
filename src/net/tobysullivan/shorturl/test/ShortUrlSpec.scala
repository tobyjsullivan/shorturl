package net.tobysullivan.shorturl.test

import org.scalatest.FlatSpec
import net.tobysullivan.shorturl.ShortUrl
import scala.collection.mutable.ArraySeq
import java.math.BigInteger
import java.security.SecureRandom
import scala.collection.mutable.ArraySeq

class ShortUrlSpec extends FlatSpec {
  "charMap" should "have 64 available characters" in {
    val mapSize = ShortUrl.CHAR_MAP.length()
    assert(mapSize == 64)
  }
  
  it should "not have any duplicate characters" in {
    val soFar = new ArraySeq[Char](ShortUrl.CHAR_MAP.length())
    
    var idx = 0
    for(curChar <- ShortUrl.CHAR_MAP.toCharArray()) {
      assert(!soFar.contains(curChar))
      soFar.update(idx, curChar)
      idx += 1
    }
  }
  
  "hashUrl" should "produce a hash from valid url" in {
    val inputUrl = "google.com"
      
    val hash = ShortUrl.hashUrl(inputUrl)
    
    assert(hash.length > 0)
    assert(hash != inputUrl)
  }
  
  it should "return distinct hashes for distinct urls" in {
    val hash1 = ShortUrl.hashUrl("domain1.com")
    val hash2 = ShortUrl.hashUrl("domain2.com")
    
    assert(hash1 != hash2)
  }
  
  it should "return the same hash for duplicate urls" in {
    val inputUrl = "example.com"
      
    val hash1 = ShortUrl.hashUrl(inputUrl)
    val hash2 = ShortUrl.hashUrl(inputUrl)
    
    assert(hash1 == hash2)
  }
  
  it should "not produce two concecutive hashes which start with the same character" in {
    val hash1 = ShortUrl.hashUrl(RandomStrings.gen(8) + ".com")
    val hash2 = ShortUrl.hashUrl(RandomStrings.gen(8) + ".com")
    
    assert(hash1.charAt(0) != hash2.charAt(0));
  }
  
  it should "produce a large number of hashes concurrently without failing" in {
    val numThreads = 1000;
    val active = new ArraySeq[Thread](numThreads)
    
    for(i <- 0 to (numThreads - 1)) {
      val thread = new Thread(new HashCreator)
      thread.start
      active.update(i, thread)
    }
    
    active.foreach(thread => thread.join())
  }
  
  "urlFromHash" should "produce the original url" in {
    
    val inputUrl = "http://" + RandomStrings.gen(8) + ".com/some/path/" + RandomStrings.gen(10)
     
    val hash = ShortUrl.hashUrl(inputUrl)
    
    val retVal = ShortUrl.urlFromHash(hash)
    assert(retVal == inputUrl)
  }
}

object RandomStrings {
  val random = new SecureRandom();

  def gen(length: Int): String = {
    new BigInteger(130, random).toString(32).substring(0, Math.min(length, 25));
  }
}

class HashCreator extends Runnable {
  def run {
    val hash = ShortUrl.hashUrl(RandomStrings.gen(14) + ".net")
    // println(hash)
  }
} 