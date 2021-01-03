package com.idirin.idceptor.utils

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.pm.PackageInfo
import android.os.Build
import android.provider.Settings
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.nio.charset.Charset
import java.util.*

object AppUtil: KoinComponent {

    private val app: Application by inject()
    private val sPref: SecurePref by inject()


    val appId = sPref.getString(PREF_DEVICE_ID, null) ?: UUID.randomUUID().toString()

    @SuppressLint("HardwareIds")
    fun getDeviceId(): String {
        val deviceId = Settings.Secure.getString(app.contentResolver, Settings.Secure.ANDROID_ID)
        return UUID.nameUUIDFromBytes(deviceId.toByteArray(Charset.forName("UTF-8"))).toString()
    }

}

fun getVersionCode(context: Context): Long {
    val pInfo: PackageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        pInfo.longVersionCode
    } else {
        pInfo.versionCode.toLong()
    }
}

fun getVersionName(context: Context): String {
    return context.packageManager.getPackageInfo(context.packageName, 0).versionName
}