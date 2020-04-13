package com.idirin.idceptor.models.network

import com.idirin.idceptor.utils.AppUtil
import com.idirin.idceptor.utils.DeviceUtil
import java.util.*


data class PostApiRequest(
    val applicationId: String = AppUtil.appId,
    val connectionId: String,
    val deviceId: String = DeviceUtil.deviceId,
    val requestPackage: RequestPackageModel? = null,
    val responsePackage: ResponsePackageModel? = null,
    val sender: String? = null,
    val tags: List<String>? = null,
    val text: String? = null
)

data class RequestPackageModel(
    val id: String,
    val body: String? = null,
    val header: String,
    val methodType: String,
    val timeStamp: Date,
    val url: String
)

data class ResponsePackageModel(
    val id: String,
    val body: String?,
    val header: String,
    val statusCode: Int,
    val timeStamp: Date
)