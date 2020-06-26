package com.idirin.idceptor.models

import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.reflect.TypeToken
import com.idirin.idceptor.utils.FormatUtils
import com.idirin.idceptor.utils.JsonConverter.instance
import okhttp3.Headers
import java.text.SimpleDateFormat
import java.util.*

@Entity(tableName = "transactions")
class HttpTransaction(
    @PrimaryKey
    val transactionId: String,
    var lastUploadedTime: Long = 0,
    var lastUpdatedTime: Long = 0
) {

    var error: String? = null
    var host: String? = null
    var method: String? = null
    var path: String? = null
    var protocol: String? = null
    var requestBody: String? = null
    var requestBodyIsPlainText = true
    var requestContentLength: Long? = null
    var requestContentType: String? = null
    var requestDate: Date? = null
    var requestHeaders: String? = null
    var responseBody: String? = null
    var responseBodyIsPlainText = true
    var responseCode: Int? = null
    var responseContentLength: Long? = null
    var responseContentType: String? = null
    var responseDate: Date? = null
    var responseHeaders: String? = null
    var responseMessage: String? = null
    var scheme: String? = null
    var tookMs: Long? = null

    var url: String? = null
        set(url) {
            field = url
            val uri = Uri.parse(url)
            host = uri.host
            path = uri.path + if (uri.query != null) "?" + uri.query else ""
            scheme = uri.scheme
        }



    val formattedRequestBody: String?
        get() = formatBody(requestBody!!, requestContentType)

    val formattedResponseBody: String?
        get() = formatBody(responseBody!!, responseContentType)


    fun setRequestHeaders(headers: Headers) {
        setRequestHeaders(toHttpHeaderList(headers))
    }

    fun setRequestHeaders(headers: List<HttpHeader?>?) {
        requestHeaders = instance.toJson(headers)
    }

    fun getRequestHeaders(): List<HttpHeader> {
        return instance.fromJson(
            requestHeaders,
            object :
                TypeToken<List<HttpHeader?>?>() {}.type
        )
    }

    fun getRequestHeaders(withMarkup: Boolean): String {
        return FormatUtils.formatHeaders(getRequestHeaders(), withMarkup)
    }



    fun setResponseHeaders(headers: Headers) {
        setResponseHeaders(toHttpHeaderList(headers))
    }

    fun setResponseHeaders(headers: List<HttpHeader?>?) {
        responseHeaders = instance.toJson(headers)
    }

    fun getResponseHeaders(): List<HttpHeader> {
        return instance.fromJson(
            responseHeaders,
            object :
                TypeToken<List<HttpHeader?>?>() {}.type
        )
    }

    fun getResponseHeaders(withMarkup: Boolean): String {
        return FormatUtils.formatHeaders(getResponseHeaders(), withMarkup)
    }



    val status: Status
        get() = when {
            error != null -> Status.Failed
            responseCode == null -> Status.Requested
            else -> Status.Complete
        }

    val requestStartTimeString: String?
        get() = if (requestDate != null) TIME_ONLY_FMT.format(
            requestDate
        ) else null

    val requestDateString: String?
        get() = if (requestDate != null) requestDate.toString() else null

    val responseDateString: String?
        get() = if (responseDate != null) responseDate.toString() else null

    val durationString: String?
        get() = if (tookMs != null) tookMs.toString() + " ms" else null

    val requestSizeString: String
        get() = formatBytes((if (requestContentLength != null) requestContentLength else 0)!!)

    val responseSizeString: String?
        get() = if (responseContentLength != null) formatBytes(responseContentLength!!) else null

    val totalSizeString: String
        get() {
            val reqBytes = (if (requestContentLength != null) requestContentLength else 0)!!
            val resBytes =
                (if (responseContentLength != null) responseContentLength else 0)!!
            return formatBytes(reqBytes + resBytes)
        }

    val responseSummaryText: String?
        get() = when (status) {
            Status.Failed -> error
            Status.Requested -> null
            else -> responseCode.toString() + " " + responseMessage
        }

    val notificationText: String
        get() = when (status) {
            Status.Failed -> " ! ! !  $path"
            Status.Requested -> " . . .  $path"
            else -> responseCode.toString() + " " + path
        }

    val isSsl: Boolean
        get() = scheme!!.toLowerCase() == "https"

    private fun toHttpHeaderList(headers: Headers): List<HttpHeader?> {
        val httpHeaders: MutableList<HttpHeader?> =
            ArrayList()
        var i = 0
        val count = headers.size()
        while (i < count) {
            httpHeaders.add(HttpHeader(headers.name(i), headers.value(i)))
            i++
        }
        return httpHeaders
    }

    private fun formatBody(body: String, contentType: String?): String? {
        return if (contentType != null && contentType.toLowerCase().contains("json")) {
            FormatUtils.formatJson(body)
        } else if (contentType != null && contentType.toLowerCase().contains("xml")) {
            FormatUtils.formatXml(body)
        } else {
            body
        }
    }

    private fun formatBytes(bytes: Long): String {
        return FormatUtils.formatByteCount(bytes, true)
    }

    companion object {
        val PARTIAL_PROJECTION = arrayOf(
            "_id",
            "requestDate",
            "tookMs",
            "method",
            "host",
            "path",
            "scheme",
            "requestContentLength",
            "responseCode",
            "error",
            "responseContentLength"
        )
        private val TIME_ONLY_FMT =
            SimpleDateFormat("HH:mm:ss", Locale.US)
    }

    enum class Status {
        Requested, Complete, Failed
    }
}



val HttpTransaction.isRequest: Boolean
    get() = (tookMs == null || responseDate == null || responseCode == null)










