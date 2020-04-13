package com.idirin.idceptor.di

import android.app.Application
import androidx.room.Room
import com.idirin.idceptor.BuildConfig
import com.idirin.idceptor.db.RestDb
import com.idirin.idceptor.db.RestDao
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Created by
 * idirin on 31.03.2020...
 */

val idDbModule: Module = module {
    single(definition = { getDb<RestDb>(androidApplication()) })
    single(definition = { getRestDao<RestDao>(getDb(androidApplication())) })
}

inline fun <reified T> getRestDao(restDb: RestDb): T {
    return restDb.restDao() as T
}

inline fun <reified T> getDb(application: Application): T {
    return Room
        .databaseBuilder(application, RestDb::class.java, BuildConfig.DB_NAME)
        .fallbackToDestructiveMigration()
        .allowMainThreadQueries()
        .build() as T
}