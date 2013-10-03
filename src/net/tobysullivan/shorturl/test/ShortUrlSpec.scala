package net.tobysullivan.shorturl.test

import org.scalatest.FlatSpec
import net.tobysullivan.shorturl.ShortUrl
import scala.collection.mutable.ArraySeq

class ShortUrlSpec extends FlatSpec {
  "charMap" should "have 64 available characters" in {
    val mapSize = ShortUrl.CHAR_MAP.length()
    assert(mapSize == 64)
  }
  
  "charMap" should "not have any duplicate characters" in {
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
  
  "hashUrl" should "return distinct hashes for distinct urls" in {
    val hash1 = ShortUrl.hashUrl("domain1.com")
    val hash2 = ShortUrl.hashUrl("domain2.com")
    
    assert(hash1 != hash2)
  }
  
  "hashUrl" should "return the same hash for duplicate urls" in {
    val inputUrl = "example.com"
      
    val hash1 = ShortUrl.hashUrl(inputUrl)
    val hash2 = ShortUrl.hashUrl(inputUrl)
    
    assert(hash1 == hash2)
  }
  
  "hashUrl" should "not produce two concecutive hashes which start with the same character" in {
    val hash1 = ShortUrl.hashUrl("ldylmyubca.com")
    val hash2 = ShortUrl.hashUrl("ewpgnahist.com")
    
    assert(hash1.charAt(0) != hash2.charAt(0));
  }
}