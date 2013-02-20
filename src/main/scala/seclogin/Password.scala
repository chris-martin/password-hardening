package seclogin

import java.nio.charset.Charset
import java.nio.CharBuffer

/** A user's unhardened password.
  */
class Password(string: String) {

  def asString: String = string

  def asChars: Array[Char] = string.toCharArray

  def asBytes: Array[Byte] =
    Charset.forName("UTF-8").encode(CharBuffer.wrap(string)).array

}

