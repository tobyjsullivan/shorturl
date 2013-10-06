package net.tobysullivan.shorturl.test

import java.math.BigInteger
import java.security.SecureRandom

object RandomHelper {
  val random = new SecureRandom();

  def genInt(max: Int): Int = {
    random.nextInt(max)
  }
  
  def gen(length: Int): String = {
    (new BigInteger(130, random)).toString(32).substring(0, Math.min(length, 25));
  }
  
  def genUrl(): String = {
    // http://randomhostname.tld/some/path/filename.ext
    "http://" + gen(12) + "." + gen(3) + "/" + gen(5) + "/" + gen(6) + "/" + gen(8) + "." + gen(3)
  }
}