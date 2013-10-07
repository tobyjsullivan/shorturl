package net.tobysullivan.shorturl.test

import scala.collection.mutable.ArraySeq

import org.scalatest.FlatSpec

import net.tobysullivan.shorturl.ShortUrl

class ShortUrlSpec extends FlatSpec {
  "CHAR_MAP" should "have 62 available characters" in {
    val mapSize = ShortUrl.CHAR_MAP.length()
    assert(mapSize == 62)
  }

  it should "not have any duplicate characters" in {
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

    val hash = ShortUrl.hashUrl(inputUrl)

    assert(hash.length > 0)
    assert(hash != inputUrl)
  }

  it should "return distinct hashes for distinct urls" in {
    val hash1 = ShortUrl.hashUrl(RandomHelper.genUrl)
    val hash2 = ShortUrl.hashUrl(RandomHelper.genUrl)

    assert(hash1 != hash2)
  }

  it should "return the same hash for duplicate urls" in {
    val inputUrl = RandomHelper.genUrl

    val hash1 = ShortUrl.hashUrl(inputUrl)
    val hash2 = ShortUrl.hashUrl(inputUrl)

    assert(hash1 == hash2)
  }

  it should "not produce two concecutive hashes which start with the same character" in {
    val hash1 = ShortUrl.hashUrl(RandomHelper.gen(8) + ".com")
    val hash2 = ShortUrl.hashUrl(RandomHelper.gen(8) + ".com")
    
    assert(hash1.charAt(0) != hash2.charAt(0));
  }

  it should "produce a large number of hashes concurrently without failing" in {
    val numThreads = 1000;
    val active = new ArraySeq[Thread](numThreads)

    for (i <- 0 to (numThreads - 1)) {
      val thread = new Thread(new HashCreator, "create-" + i)
      thread.start
      active.update(i, thread)
    }

    active.foreach(thread => thread.join())
  }

  "urlFromHash" should "produce the original url many times" in {
    for (i <- 1 to 1000) {
      val inputUrl = "http://" + RandomHelper.gen(8) + ".com"

      val hash = ShortUrl.hashUrl(inputUrl)

      val retVal = ShortUrl.urlFromHash(hash)

      
      assert(retVal == inputUrl)
    }
  }

  it should "produce the original url" in {

    val inputUrl = "http://" + RandomHelper.gen(8) + ".com/some/path/" + RandomHelper.gen(10)

    val hash = ShortUrl.hashUrl(inputUrl)

    val retVal = ShortUrl.urlFromHash(hash)
    assert(retVal == inputUrl)
  }

  it should "round-trip a large number of hashes concurrently without failing" in {
    val numThreads = 1000;
    val active = new ArraySeq[Thread](numThreads)

    for (i <- 0 to (numThreads - 1)) {

      val thread = new Thread(new HashRoundTripper, "roundtrip-" + i)
      thread.start
      active.update(i, thread)
    }

    active.foreach(thread => thread.join())
  }

  "statsFor" should "return 0 clicks on a new hash" in {
    val hash = ShortUrl.hashUrl("http://www." + RandomHelper.gen(8) + ".net/")

    val stats = ShortUrl.statsFor(hash)

    val clicks = stats.get(ShortUrl.STATS_CLICKS)

    assert(clicks.isDefined)

    assert(clicks.get == 0)
  }

  "statsFor" should "return correct number of clicks for a hash" in {
    val hash = ShortUrl.hashUrl("http://www." + RandomHelper.gen(8) + ".biz")

    // Click/lookup several times
    val numClicks = 25
    for (i <- 1 to numClicks) {
      val throwAway = ShortUrl.urlFromHash(hash)
    }

    val stats = ShortUrl.statsFor(hash)

    val clicks = stats.get(ShortUrl.STATS_CLICKS)

    assert(clicks.isDefined)

    assert(clicks.get == numClicks)
  }
}

class HashCreator extends Runnable {
  def run {
    val hash = ShortUrl.hashUrl(RandomHelper.gen(14) + ".net")

    assert(hash.length() > 0)
  }
}

class HashRoundTripper extends Runnable {
  def run {
    val inputUrl = "http://" + RandomHelper.gen(14) + ".org"

    val hash = ShortUrl.hashUrl(inputUrl)

    val resultUrl = ShortUrl.urlFromHash(hash)

    assert(inputUrl == resultUrl)
  }
} 