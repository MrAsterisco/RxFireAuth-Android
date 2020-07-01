package io.github.mrasterisco.rxfireauth.handlers.apple

import java.nio.ByteBuffer
import java.nio.CharBuffer
import java.nio.charset.CodingErrorAction
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.SecureRandom

fun String.Companion.getSecureRandomString(length: Int = 32): String {
    val generator = SecureRandom()

    val charsetDecoder = StandardCharsets.US_ASCII.newDecoder()
    charsetDecoder.onUnmappableCharacter(CodingErrorAction.IGNORE)
    charsetDecoder.onMalformedInput(CodingErrorAction.IGNORE)

    val bytes = ByteArray(length)
    val inBuffer = ByteBuffer.wrap(bytes)
    val outBuffer = CharBuffer.allocate(length)
    while (outBuffer.hasRemaining()) {
        generator.nextBytes(bytes)
        inBuffer.rewind()
        charsetDecoder.reset()
        charsetDecoder.decode(inBuffer, outBuffer, false)
    }
    outBuffer.flip()
    return outBuffer.toString()
}

fun String.sha256(): String {
    val md = MessageDigest.getInstance("SHA-256")
    val digest = md.digest(toByteArray())
    val hash = StringBuilder()
    for (c in digest) {
        hash.append(String.format("%02x", c))
    }
    return hash.toString()
}