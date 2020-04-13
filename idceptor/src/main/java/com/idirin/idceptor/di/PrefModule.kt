package com.idirin.idceptor.di

import android.app.Application
import com.idirin.idceptor.utils.SecurePref
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Created by
 * idirin on 01.04.2020...
 */

val idPrefModule: Module = module{
    //single( definition = { getPref<SharedPreferences>(androidApplication()) })
    single( definition = { getSecurePref<SecurePref>(androidApplication())})
}

//inline fun <reified T> getPref(application: Application): T {
//    return PreferenceManager.getDefaultSharedPreferences(application) as T
//}

inline fun <reified T> getSecurePref(application: Application): T {
    return SecurePref(application) as T
}