package com.idirin.idceptor.network

import com.idirin.idceptor.BuildConfig
import com.idirin.idceptor.utils.AppUtil.getDeviceId
import okhttp3.Interceptor
import okhttp3.Response
import org.koin.core.KoinComponent
import java.io.IOException

/**
 * Created by
 * idirin on 01.04.2020...
 */

class AlgClientIdInterceptor(private val appKey: String) : Interceptor, KoinComponent {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()

        val  request = original.newBuilder()
                .addHeader("Alg-Client-Id", BuildConfig.CLIENT_ID)
                .addHeader("Alg-App-Key", appKey)
                .addHeader("Alg-Device-UUId", getDeviceId())
                .method(original.method(), original.body())
                .build()

        return chain.proceed(request)
    }

}