package net.tobysullivan.shorturl.test

import scala.collection.mutable.ArraySeq

import org.scalatest.FlatSpec

import net.tobysullivan.shorturl.ShortUrl

class ShortUrlSpec extends FlatSpec {
  def fixture =
    new {
      val shorturl = new ShortUrl(Configuration.HASH_STORE, Configuration.STATS_STORE)
    }
  
  "CHAR_MAP" should "have 62 available characters" in {
    val mapSize = ShortUrl.CHAR_MAP.length()
    assert(mapSize == 62)
  }

  it should "not have any duplicate characters" in {
    val shorturl = fixture.shorturl
    val soFar = new ArraySeq[Char](ShortUrl.CHAR_MAP.length())

    var idx = 0
    for (curChar <- ShortUrl.CHAR_MAP.toCharArray()) {
      assert(!soFar.contains(curChar))
      soFar.update(idx, curChar)
      idx += 1
    }
  }

  "hashUrl" should "produce a hash from valid url" in {
    val inputUrl = RandomHelper.genUrl

    val hash = fixture.shorturl.hashUrl(inputUrl)

    assert(hash.length > 0)
    assert(hash != inputUrl)
  }

  it should "return distinct hashes for distinct urls" in {
    val shorturl = fixture.shorturl
    
    val hash1 = shorturl.hashUrl(RandomHelper.genUrl)
    val hash2 = shorturl.hashUrl(RandomHelper.genUrl)

    assert(hash1 != hash2)
  }

  it should "return the same hash for duplicate urls" in {
    val shorturl = fixture.shorturl
    
    val inputUrl = RandomHelper.genUrl

    val hash1 = shorturl.hashUrl(inputUrl)
    val hash2 = shorturl.hashUrl(inputUrl)

    assert(hash1 == hash2)
  }

  it should "not produce two concecutive hashes which start with the same character" in {
    val shorturl = fixture.shorturl
    
    val hash1 = shorturl.hashUrl(RandomHelper.gen(8) + ".com")
    val hash2 = shorturl.hashUrl(RandomHelper.gen(8) + ".com")
    
    assert(hash1.charAt(0) != hash2.charAt(0));
  }

  it should "produce a large number of hashes concurrently without failing" in {
    val numThreads = 100;
    val active = new ArraySeq[Thread](numThreads)

    for (i <- 0 to (numThreads - 1)) {
      val thread = new Thread(new HashCreator(fixture.shorturl), "create-" + i)
      thread.start
      active.update(i, thread)
    }

    active.foreach(thread => thread.join())
  }

  "urlFromHash" should "produce the original url many times" in {
    val shorturl = fixture.shorturl
    
    for (i <- 1 to 1000) {
      val inputUrl = "http://" + RandomHelper.gen(8) + ".com"

      val hash = shorturl.hashUrl(inputUrl)

      val retVal = shorturl.urlFromHash(hash)

      
      assert(retVal == inputUrl)
    }
  }

  it should "produce the original url" in {
    val shorturl = fixture.shorturl

    val inputUrl = "http://" + RandomHelper.gen(8) + ".com/some/path/" + RandomHelper.gen(10)

    val hash = shorturl.hashUrl(inputUrl)

    val retVal = shorturl.urlFromHash(hash)
    assert(retVal == inputUrl)
  }

  it should "round-trip a large number of hashes concurrently without failing" in {
    val numThreads = 100;
    val active = new ArraySeq[Thread](numThreads)

    for (i <- 0 to (numThreads - 1)) {

      val thread = new Thread(new HashRoundTripper(fixture.shorturl), "roundtrip-" + i)
      thread.start
      active.update(i, thread)
    }

    active.foreach(thread => thread.join())
  }

  "statsFor" should "return 0 clicks on a new hash" in {
    val shorturl = fixture.shorturl
    
    val hash = shorturl.hashUrl("http://www." + RandomHelper.gen(8) + ".net/")

    val stats = shorturl.statsFor(hash)

    val clicks = stats.get(ShortUrl.STATS_CLICKS)

    assert(clicks.isDefined)

    assert(clicks.get == 0)
  }

  "statsFor" should "return correct number of clicks for a hash" in {
    val shorturl = fixture.shorturl
    
    val hash = shorturl.hashUrl("http://www." + RandomHelper.gen(8) + ".biz")

    // Click/lookup several times
    val numClicks = 25
    for (i <- 1 to numClicks) {
      val throwAway = shorturl.urlFromHash(hash)
    }

    val stats = shorturl.statsFor(hash)

    val clicks = stats.get(ShortUrl.STATS_CLICKS)

    assert(clicks.isDefined)

    assert(clicks.get == numClicks)
  }
}

class HashCreator(shorturl: ShortUrl) extends Runnable {
  def run {
    val hash = shorturl.hashUrl(RandomHelper.gen(14) + ".net")

    assert(hash.length() > 0)
  }
}

class HashRoundTripper(shorturl: ShortUrl) extends Runnable {
  def run {
    val inputUrl = "http://" + RandomHelper.gen(14) + ".org"

    val hash = shorturl.hashUrl(inputUrl)

    val resultUrl = shorturl.urlFromHash(hash)

    assert(inputUrl == resultUrl)
  }
} 