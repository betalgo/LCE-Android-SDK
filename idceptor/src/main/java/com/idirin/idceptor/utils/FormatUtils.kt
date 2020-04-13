package com.idirin.idceptor.utils

import android.content.Context
import android.text.TextUtils
import com.google.gson.JsonParser
import com.idirin.idceptor.R
import com.idirin.idceptor.models.HttpHeader
import com.idirin.idceptor.models.HttpTransaction
import com.idirin.idceptor.utils.JsonConverter.instance
import org.xml.sax.InputSource
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.*
import javax.xml.transform.OutputKeys
import javax.xml.transform.Source
import javax.xml.transform.sax.SAXSource
import javax.xml.transform.sax.SAXTransformerFactory
import javax.xml.transform.stream.StreamResult

object FormatUtils {
    fun formatHeaders(
        httpHeaders: List<HttpHeader>?,
        withMarkup: Boolean
    ): String {
        var out = ""
        if (httpHeaders != null) {
            for (header in httpHeaders) {
                out += (if (withMarkup) "<b>" else "") + header.name + ": " + (if (withMarkup) "</b>" else "") +
                        header.value + if (withMarkup) "<br />" else "\n"
            }
        }
        return out
    }

    fun formatByteCount(bytes: Long, si: Boolean): String {
        val unit = if (si) 1000 else 1024
        if (bytes < unit) return "$bytes B"
        val exp =
            (Math.log(bytes.toDouble()) / Math.log(unit.toDouble())).toInt()
        val pre =
            (if (si) "kMGTPE" else "KMGTPE")[exp - 1].toString() + if (si) "" else "i"
        return String.format(
            Locale.US,
            "%.1f %sB",
            bytes / Math.pow(unit.toDouble(), exp.toDouble()),
            pre
        )
    }

    fun formatJson(json: String): String {
        return try {
            val jp = JsonParser()
            val je = jp.parse(json)
            instance.toJson(je)
        } catch (e: Exception) {
            json
        }
    }

    fun formatXml(xml: String): String {
        return try {
            val serializer =
                SAXTransformerFactory.newInstance().newTransformer()
            serializer.setOutputProperty(OutputKeys.INDENT, "yes")
            serializer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2")
            val xmlSource: Source = SAXSource(
                InputSource(
                    ByteArrayInputStream(xml.toByteArray())
                )
            )
            val res =
                StreamResult(ByteArrayOutputStream())
            serializer.transform(xmlSource, res)
            String((res.outputStream as ByteArrayOutputStream).toByteArray())
        } catch (e: Exception) {
            xml
        }
    }

    fun getShareCurlCommand(transaction: HttpTransaction): String {
        var compressed = false
        var curlCmd = "curl"
        curlCmd += " -X " + transaction.method
        val headers =
            transaction.getRequestHeaders()
        var i = 0
        val count = headers.size
        while (i < count) {
            val name = headers[i].name
            val value = headers[i].value
            if ("Accept-Encoding".equals(name, ignoreCase = true) && "gzip".equals(
                    value,
                    ignoreCase = true
                )
            ) {
                compressed = true
            }
            curlCmd += " -H \"$name: $value\""
            i++
        }
        val requestBody = transaction.requestBody
        if (requestBody != null && requestBody.length > 0) {
            // try to keep to a single line and use a subshell to preserve any line breaks
            curlCmd += " --data $'" + requestBody.replace("\n", "\\n") + "'"
        }
        curlCmd += (if (compressed) " --compressed " else " ") + transaction.url
        return curlCmd
    }

    private fun v(string: String?): String {
        return string ?: ""
    }
}