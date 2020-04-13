package com.idirin.idceptor.di

import android.app.Application
import com.google.gson.GsonBuilder
import com.idirin.idceptor.BuildConfig
import com.idirin.idceptor.IDCeptor
import com.idirin.idceptor.network.AlgClientIdInterceptor
import com.idirin.idceptor.network.IdApiInterface
import com.idirin.idceptor.network.IdTypeAdapterDate
import com.idirin.idceptor.utils.DeviceUtil
import com.idirin.idceptor.utils.USER_AGENT
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.Module
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import java.util.concurrent.TimeUnit

val idApiModule: Module = module{
    single { getApi<IdApiInterface>(androidApplication(), IDCeptor.apiKey) }
}

inline fun <reified T> getApi(application: Application, apiKey: String): T {

    val gson = GsonBuilder()
        .registerTypeAdapter(Date::class.java, IdTypeAdapterDate())
        .setLenient()
        .create()

    val cacheSize: Long = 10 * 1024 * 1024  //10mb cache size
    val cache = Cache(application.cacheDir, cacheSize)

    val userAgentInterceptor = Interceptor { chain ->
        val request = chain.request()
        val requestWithUserAgent = request.newBuilder()
            .header(USER_AGENT, DeviceUtil.userAgent)
            .build()
        chain.proceed(requestWithUserAgent)
    }

    val okHttpClient = okhttp3.OkHttpClient.Builder()
        .connectTimeout(BuildConfig.CONNECTION_TIMEOUT.toLong(), TimeUnit.MILLISECONDS)
        .readTimeout(BuildConfig.CONNECTION_TIMEOUT.toLong(), TimeUnit.MILLISECONDS)
        .writeTimeout(BuildConfig.CONNECTION_UPLOAD_TIMEOUT.toLong(), TimeUnit.MILLISECONDS)
        .cache(cache)
        .addInterceptor(userAgentInterceptor)
        .addInterceptor(AlgClientIdInterceptor(apiKey))

    if (BuildConfig.DEBUG) {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        okHttpClient.addInterceptor(loggingInterceptor)
    }

    return Retrofit.Builder()
        .baseUrl(BuildConfig.REST_ENDPOINT)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .client(okHttpClient.build())
        .build()
        .create(T::class.java)
}