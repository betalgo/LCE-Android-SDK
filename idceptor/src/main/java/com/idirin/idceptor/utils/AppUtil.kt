package com.idirin.idceptor.utils

import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.*


object AppUtil: KoinComponent {

    private val sPref: SecurePref by inject()


    val appId = sPref.getString(PREF_DEVICE_ID, null) ?: UUID.randomUUID().toString()




}