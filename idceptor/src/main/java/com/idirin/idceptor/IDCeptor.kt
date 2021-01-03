package com.idirin.idceptor

import android.app.Application
import com.idirin.idceptor.db.DbHelper
import com.idirin.idceptor.di.idApiModule
import com.idirin.idceptor.di.idConcurrencyModule
import com.idirin.idceptor.di.idDbModule
import com.idirin.idceptor.di.idPrefModule
import com.idirin.idceptor.models.HttpTransaction
import com.idirin.idceptor.network.UploadHelper
import com.idirin.idceptor.utils.*
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.internal.http.HttpHeaders
import okio.Buffer
import okio.BufferedSource
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.*
import java.nio.charset.Charset
import java.nio.charset.UnsupportedCharsetException
import java.util.*
import java.util.concurrent.TimeUnit

class IDCeptor(private val application: Application, apiKey: String): Interceptor {

    init {
        IDCeptor.apiKey = apiKey

        val modules = listOf(idApiModule, idDbModule, idPrefModule, idConcurrencyModule)
        if (KoinContextHandler.getOrNull() == null) {
            startKoin {
                androidContext(application)
                modules(modules)
            }
        } else {
            unloadKoinModules(modules)
            loadKoinModules(modules)
        }

        UploadHelper.init()
    }

    companion object {
        private val UTF8 = Charset.forName("UTF-8")

        lateinit var apiKey: String
    }

    private val maxContentLength = 250000L

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        val requestBody = request.body()
        val hasRequestBody = requestBody != null

        val transaction = HttpTransaction(DbHelper.generateGUID())
        transaction.requestDate = Date()
        transaction.method = request.method()
        transaction.url = request.url().toString()

        transaction.setRequestHeaders(request.headers())
        if (hasRequestBody) {
            if (requestBody!!.contentType() != null) {
                transaction.requestContentType = requestBody.contentType().toString()
            }
            if (requestBody.contentLength() != -1L) {
                transaction.requestContentLength = requestBody.contentLength()
            }
        }

        transaction.requestBodyIsPlainText = !bodyHasUnsupportedEncoding(request.headers())
        if (hasRequestBody && transaction.requestBodyIsPlainText) {
            val source: BufferedSource = getNativeSource(Buffer(), bodyGzipped(request.headers()))
            val buffer = source.buffer()
            requestBody!!.writeTo(buffer)
            var charset: Charset = UTF8
            val contentType = requestBody.contentType()
            if (contentType != null) {
                charset = contentType.charset(UTF8)!!
            }
            if (isPlaintext(buffer)) {
                transaction.requestBody = readFromBuffer(application, maxContentLength, buffer, charset)
            } else {
                transaction.responseBodyIsPlainText = false
            }
        }

        DbHelper.create(transaction)

        val startNs = System.nanoTime()
        val response: Response
        response = try {
            chain.proceed(request)
        } catch (e: Exception) {
            transaction.error = e.toString()
            DbHelper.update(transaction)
            throw e
        }
        val tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs)

        val responseBody = response.body()

        transaction.setRequestHeaders(
            response.request().headers()
        ) // includes headers added later in the chain

        transaction.responseDate = Date()
        transaction.tookMs = tookMs
        transaction.protocol = response.protocol().toString()
        transaction.responseCode = response.code()
        transaction.responseMessage = response.message()

        transaction.responseContentLength = responseBody!!.contentLength()
        if (responseBody.contentType() != null) {
            transaction.responseContentType = responseBody.contentType().toString()
        }
        transaction.setResponseHeaders(response.headers())

        transaction.responseBodyIsPlainText = !bodyHasUnsupportedEncoding(response.headers())
        if (HttpHeaders.hasBody(response) && transaction.responseBodyIsPlainText) {
            val source: BufferedSource = getNativeSource(response, maxContentLength)
            source.request(Long.MAX_VALUE)
            val buffer = source.buffer()
            var charset: Charset = UTF8
            val contentType = responseBody.contentType()
            if (contentType != null) {
                try {
                    charset = contentType.charset(UTF8)!!
                } catch (e: UnsupportedCharsetException) {
                    DbHelper.update(transaction)
                    return response
                }
            }
            if (isPlaintext(buffer)) {
                transaction.responseBody = readFromBuffer(application, maxContentLength, buffer.clone(), charset)
            } else {
                transaction.responseBodyIsPlainText = false
            }
            transaction.responseContentLength = buffer.size()
        }

        DbHelper.update(transaction)

        return response
    }








}