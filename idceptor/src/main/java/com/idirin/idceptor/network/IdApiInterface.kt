package com.idirin.idceptor.network

import com.idirin.idceptor.models.network.AppInitRequest
import com.idirin.idceptor.models.network.PostApiRequest
import kotlinx.coroutines.Deferred
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

/**
 * Created by
 * idirin on 18/12/2017...
 */

interface IdApiInterface {

    @POST("Data/SendPackage")
    fun postApi(@Body request: PostApiRequest): Call<ResponseBody>


    @PUT("App/UpdateSubApp")
    fun initApp(@Body request: AppInitRequest): Call<ResponseBody>


}
























































