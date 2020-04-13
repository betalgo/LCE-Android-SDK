package com.idirin.idceptor.network

import okhttp3.Interceptor
import okhttp3.Response
import org.koin.core.KoinComponent
import java.io.IOException

/**
 * Created by
 * idirin on 01.04.2020...
 */

class AlgClientIdInterceptor(private val apiKey: String) : Interceptor, KoinComponent {

    //private const val client_secret = "424F5699-9FBA-4161-AE79-49839455E04A"

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()

        val  request = original.newBuilder()
                .addHeader("Alg-Client-Id", apiKey)
                .method(original.method(), original.body())
                .build()

        return chain.proceed(request)
    }

}