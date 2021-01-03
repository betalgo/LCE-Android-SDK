package com.idirin.idceptor.models.network

import com.idirin.idceptor.utils.AppUtil
import java.util.*

data class PostApiRequest(
        val connectionId: String,
        val deviceUUID: String = AppUtil.getDeviceId(),
        val requestPackage: RequestPackageModel? = null,
        val responsePackage: ResponsePackageModel? = null,
        val sender: String? = null,
        val tags: List<String>? = null,
        val text: String? = null
)

data class RequestPackageModel(
    val id: String,
    val body: String? = null,
    val headers: List<String>,
    val methodType: String,
    val timeStamp: Date,
    val url: String
)

data class ResponsePackageModel(
    val id: String,
    val body: String?,
    val headers: List<String>,
    val statusCode: Int,
    val timeStamp: Date
)