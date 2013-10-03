package net.tobysullivan.shorturl

object ShortUrl {
  // The available characters for producing a hash. The more characters we use, the shorter we can keep our hashes. However,
  // it is important to strike a balance with usability. For example: short.url/ck&:*~al is probably not a very good hash.
  val CHAR_MAP = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ_-"
  
  def hashUrl(url: String): String = {
    hashFromInt(getNextCursor())
  }

  def urlFromHash(hash: String): String = {
    "TODO"
  }

  def statsFor(hash: String): Map[String, Any] = {
    Map[String, Any]()
  }
  
  private var cursor = 1;
  private def getNextCursor(): Int = {
    cursor += 1
    cursor
  }
  
  private def intFromHash(hash: String): Int = {
    val mapSize = this.CHAR_MAP.size
    var out = 0
     
    for(curChar <- hash.reverse.toCharArray()) {
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
    
    out.reverse
  }
}