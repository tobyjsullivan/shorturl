package net.tobysullivan.shorturl.test

import org.scalatest.FlatSpec
import net.tobysullivan.shorturl.DuplicateHashException
import net.tobysullivan.shorturl.HashStore
import net.tobysullivan.shorturl.AvailableHashManager

class HashStoreSpec extends FlatSpec {
  def fixture =
    new {
      val hashStore = Configuration.HASH_STORE
      val hashManager = new AvailableHashManager()(hashStore)
    }

  "addHashUrlPair" should "not fail to add a pair with a unique hash" in {
    val hash = fixture.hashManager.getNext

    val url = RandomHelper.genUrl

    HashStore.addHashUrlPair(hash, url)(fixture.hashStore)
  }

  "findUrl" should "find a URL for a given hash url pair that was just added" in {
    val hash = fixture.hashManager.getNext

    val inputUrl = RandomHelper.genUrl

    HashStore.addHashUrlPair(hash, inputUrl)(fixture.hashStore)

    val foundUrl = HashStore.findUrl(hash)(fixture.hashStore)

    assert(foundUrl.isDefined)

    assert(foundUrl.get == inputUrl)
  }

  it should "return None for a hash that was never added" in {
    val url = HashStore.findUrl(Int.MaxValue - 1)(fixture.hashStore)

    assert(url.isEmpty)

    assert(url == None)
  }

  "findHash" should "locate the hash for a URL pair that was just added" in {
    val inputHash = fixture.hashManager.getNext

    val url = RandomHelper.genUrl

    HashStore.addHashUrlPair(inputHash, url)(fixture.hashStore)

    val foundHash = HashStore.findHash(url)(fixture.hashStore)

    assert(foundHash.isDefined)

    assert(foundHash.get == inputHash)
  }

  it should "return None for a url that was never added" in {
    val hash = HashStore.findHash("thisurlmustneverbeadded")(fixture.hashStore)

    assert(hash.isEmpty)

    assert(hash == None)
  }
}