package net.tobysullivan.shorturl.test

import org.scalatest.FlatSpec

import net.tobysullivan.shorturl.DuplicateHashException
import net.tobysullivan.shorturl.HashStore

class HashStoreSpec extends FlatSpec {
	"addHashUrlPair" should "not fail to add a pair with a unique hash" in {
	  val hash = HashStore.findNextAvailableHash
	  
	  val url = RandomStrings.genUrl
	  
	  HashStore.addHashUrlPair(hash, url)
	}
	
	it should "throw an exception if a duplicate hash is added" in {
	  val hash = HashStore.findNextAvailableHash
	  
	  val url1 = RandomStrings.genUrl
	  
	  val url2 = RandomStrings.genUrl
	  
	  HashStore.addHashUrlPair(hash, url1)
	  
	  intercept[DuplicateHashException] {
	    HashStore.addHashUrlPair(hash, url2)
	  }
	  
	}
	
	"findUrl" should "find a URL for a given hash url pair that was just added" in {
	  val hash = HashStore.findNextAvailableHash
	  
	  val inputUrl = RandomStrings.genUrl
	  
	  HashStore.addHashUrlPair(hash, inputUrl)
	  
	  val foundUrl = HashStore.findUrl(hash)
	  
	  assert(foundUrl.isDefined)
	  
	  assert(foundUrl.get == inputUrl)
	}
	
	it should "return None for a hash that was never added" in {
	  val url = HashStore.findUrl(Int.MaxValue - 1)
	  
	  assert(url.isEmpty)
	  
	  assert(url == None)
	}
	
	"findHash" should "locate the hash for a URL pair that was just added" in {
	  val inputHash = HashStore.findNextAvailableHash
	  
	  val url = RandomStrings.genUrl
	  
	  HashStore.addHashUrlPair(inputHash, url)
	  
	  val foundHash = HashStore.findHash(url)
	  
	  assert(foundHash.isDefined)
	  
	  assert(foundHash.get == inputHash)
	}
	
	it should "return None for a url that was never added" in {
	  val hash = HashStore.findHash("thisurlmustneverbeadded")
	  
	  assert(hash.isEmpty)
	  
	  assert(hash == None)
	}
}