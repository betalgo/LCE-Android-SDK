package com.idirin.idceptor.utils

import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import com.idirin.idceptor.BuildConfig
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.ObjectOutputStream
import java.text.DecimalFormat
import java.util.*

object DeviceUtil: KoinComponent {



    private val sPref: SecurePref by inject()

    val deviceId = sPref.getString(PREF_DEVICE_ID, null) ?: UUID.randomUUID().toString()


//    val userAgent: String
//        get() = ("Android/" + BuildConfig.APPLICATION_ID + "/" + BuildConfig.VERSION_CODE + "/("
//                + Build.MANUFACTURER + ":" + Build.VERSION.RELEASE + ":" + Build.MODEL + ")")

    fun bytesToHuman(size: Long): String {
        val Kb: Long = 1024
        val Mb = Kb * 1024
        val Gb = Mb * 1024
        val Tb = Gb * 1024
        val Pb = Tb * 1024
        val Eb = Pb * 1024

        if (size < Kb) return floatForm(size.toDouble()) + " byte"
        if (size in Kb..(Mb - 1)) return floatForm(size.toDouble() / Kb) + " Kb"
        if (size in Mb..(Gb - 1)) return floatForm(size.toDouble() / Mb) + " Mb"
        if (size in Gb..(Tb - 1)) return floatForm(size.toDouble() / Gb) + " Gb"
        if (size in Tb..(Pb - 1)) return floatForm(size.toDouble() / Tb) + " Tb"
        if (size in Pb..(Eb - 1)) return floatForm(size.toDouble() / Pb) + " Pb"
        return if (size >= Eb) floatForm(size.toDouble() / Eb) + " Eb" else "???"
    }

    private fun floatForm(d: Double): String {
        return DecimalFormat("#.##").format(d)
    }

    fun isPackageInstalled(application: Application, uri: String): Boolean {
        return try {
            val pm = application.packageManager
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES).activities != null
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    fun getNavigationBarHeight(context: Context): Int {
        val resources = context.resources
        val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        return if (resourceId > 0) {
            resources.getDimensionPixelSize(resourceId)
        } else 0
    }




    fun getDevicePlatform(): String {
        return "android"
    }

    fun getDeviceId(context: Context): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }

    fun getDeviceManufacturer(): String {
        return Build.MANUFACTURER
    }

    fun getDeviceModel(): String {
        return Build.MODEL
    }

    fun getDeviceOSVersion(): String {
        return Build.VERSION.CODENAME
    }

    fun getRandomUUID(): String {
        return UUID.randomUUID().toString()
    }





    fun getAcitivites() {
        /*
        ActivityInfo[] list = getPackageManager().getPackageInfo(getPackageName()).activities;
         */
    }

    @Throws(IOException::class)
    fun sizeof(obj: Any): Int {

        val byteOutputStream = ByteArrayOutputStream()
        val objectOutputStream = ObjectOutputStream(byteOutputStream)

        objectOutputStream.writeObject(obj)
        objectOutputStream.flush()
        objectOutputStream.close()

        return byteOutputStream.toByteArray().size
    }



    /*
val mainIntent = Intent(Intent.ACTION_MAIN, null)
mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)
val pkgAppsList = packageManager.queryIntentActivities(mainIntent, 0)
for (app in pkgAppsList) {
    Log.i("InstalledPackages", app.activityInfo.packageName)
}
*/

}
