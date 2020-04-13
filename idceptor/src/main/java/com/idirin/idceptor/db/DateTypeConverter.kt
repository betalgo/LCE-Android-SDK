package com.idirin.idceptor.db

import androidx.room.TypeConverter
import java.util.*

/**
 * Created by
 * idirin on 30/03/2020...
 */

class DateTypeConverter {

    @TypeConverter
    fun toDate(value: Long?): Date? {
        return if (value == null) null else Date(value)
    }

    @TypeConverter
    fun toLong(value: Date?): Long? {
        return value?.time
    }
}
