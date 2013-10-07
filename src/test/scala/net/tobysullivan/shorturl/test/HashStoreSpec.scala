package net.tobysullivan.shorturl.test

import org.scalatest.FlatSpec
import net.tobysullivan.shorturl.DuplicateHashException
import net.tobysullivan.shorturl.HashStore
import net.tobysullivan.shorturl.AvailableHashManager

class HashStoreSpec extends FlatSpec {
  def fixture =
    new {
      val hashStore = Configuration.HASH_STORE
      val hashManager = new AvailableHashManager(hashStore)
    }

  "addHashUrlPair" should "not fail to add a pair with a unique hash" in {
    val hash = fixture.hashManager.getNext

    val url = RandomHelper.genUrl

    fixture.hashStore.addHashUrlPair(hash, url)
  }

  "findUrl" should "find a URL for a given hash url pair that was just added" in {
    val hash = fixture.hashManager.getNext

    val inputUrl = RandomHelper.genUrl

    fixture.hashStore.addHashUrlPair(hash, inputUrl)

    val foundUrl = fixture.hashStore.findUrl(hash)

    assert(foundUrl.isDefined)

    assert(foundUrl.get == inputUrl)
  }

  it should "return None for a hash that was never added" in {
    val url = fixture.hashStore.findUrl(Int.MaxValue - 1)

    assert(url.isEmpty)

    assert(url == None)
  }

  "findHash" should "locate the hash for a URL pair that was just added" in {
    val inputHash = fixture.hashManager.getNext

    val url = RandomHelper.genUrl

    fixture.hashStore.addHashUrlPair(inputHash, url)

    val foundHash = fixture.hashStore.findHash(url)

    assert(foundHash.isDefined)

    assert(foundHash.get == inputHash)
  }

  it should "return None for a url that was never added" in {
    val hash = fixture.hashStore.findHash("thisurlmustneverbeadded")

    assert(hash.isEmpty)

    assert(hash == None)
  }
}