package com.idirin.idceptor.utils

import android.content.Context
import android.util.Log
import com.idirin.idceptor.R
import okhttp3.Headers
import okhttp3.Response
import okio.Buffer
import okio.BufferedSource
import okio.GzipSource
import okio.Okio
import java.io.EOFException
import java.io.IOException
import java.nio.charset.Charset

/**
 * Returns true if the body in question probably contains human readable text. Uses a small sample
 * of code points to detect unicode control characters commonly used in binary file signatures.
 */
fun isPlaintext(buffer: Buffer): Boolean {
    return try {
        val prefix = Buffer()
        val byteCount = if (buffer.size() < 64) buffer.size() else 64
        buffer.copyTo(prefix, 0, byteCount)
        for (i in 0..15) {
            if (prefix.exhausted()) {
                break
            }
            val codePoint = prefix.readUtf8CodePoint()
            if (Character.isISOControl(codePoint) && !Character.isWhitespace(
                    codePoint
                )
            ) {
                return false
            }
        }
        true
    } catch (e: EOFException) {
        false // Truncated UTF-8 sequence.
    }
}

fun getNativeSource(input: BufferedSource, isGzipped: Boolean): BufferedSource {
    return if (isGzipped) {
        val source = GzipSource(input)
        Okio.buffer(source)
    } else {
        input
    }
}

@Throws(IOException::class)
fun getNativeSource(response: Response, maxContentLength: Long): BufferedSource {
    if (bodyGzipped(response.headers())) {
        val source = response.peekBody(maxContentLength).source()
        if (source.buffer().size() < maxContentLength) {
            return getNativeSource(source, true)
        } else {
            Log.w(LOG_TAG, "gzip encoded response was too long")
        }
    }
    return response.body()!!.source()
}

fun bodyHasUnsupportedEncoding(headers: Headers): Boolean {
    val contentEncoding = headers["Content-Encoding"]
    return contentEncoding != null &&
            !contentEncoding.equals("identity", ignoreCase = true) &&
            !contentEncoding.equals("gzip", ignoreCase = true)
}

fun bodyGzipped(headers: Headers): Boolean {
    val contentEncoding = headers["Content-Encoding"]
    return "gzip".equals(contentEncoding, ignoreCase = true)
}

fun readFromBuffer(context: Context, maxContentLength: Long, buffer: Buffer, charset: Charset): String? {
    val bufferSize = buffer.size()
    val maxBytes = Math.min(bufferSize, maxContentLength)
    var body: String? = ""
    try {
        body = buffer.readString(maxBytes, charset)
    } catch (e: EOFException) {
        body += context.getString(R.string.body_unexpected_eof)
    }
    if (bufferSize > maxContentLength) {
        body += context.getString(R.string.body_content_truncated)
    }
    return body
}
















