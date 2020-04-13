package com.idirin.idceptor.utils

import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.internal.bind.DateTypeAdapter
import java.util.*

object JsonConverter {

    private var gson: Gson? = null

    val instance: Gson
        get() {
            if (gson == null) {
                gson = GsonBuilder()
                    .setPrettyPrinting()
                    .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                    .registerTypeAdapter(Date::class.java, DateTypeAdapter())
                    .create()
            }
            return gson!!
        }
}