package com.idirin.idceptor.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.idirin.idceptor.BuildConfig
import com.idirin.idceptor.models.HttpTransaction

/**
 * Created by
 * idirin on 30.03.2020...
 */

@Database(
    entities = [HttpTransaction::class], version = BuildConfig.DB_VERSION
)
@TypeConverters(DateTypeConverter::class)
abstract class RestDb : RoomDatabase() {

    abstract fun restDao(): RestDao
}