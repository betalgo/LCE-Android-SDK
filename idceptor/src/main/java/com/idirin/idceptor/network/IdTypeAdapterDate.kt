package com.idirin.idceptor.network

import android.util.Log
import com.google.gson.*
import java.lang.reflect.Type
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class IdTypeAdapterDate : JsonSerializer<Date>, JsonDeserializer<Date> {

    companion object {
        private val DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
        private val TIME_ZONE = "GMT+3:00"
    }

    @Throws(JsonParseException::class)
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Date? {
        val dateStr = json.asString

        val formatter = SimpleDateFormat(DATE_FORMAT)
        formatter.timeZone = TimeZone.getTimeZone(TIME_ZONE)

        return tryDeserialize(formatter, dateStr)
    }

    override fun serialize(date: Date, type: Type, context: JsonSerializationContext): JsonElement {
        val formatter = SimpleDateFormat(DATE_FORMAT)
        formatter.timeZone = TimeZone.getTimeZone(TIME_ZONE)
        val str = formatter.format(date)
        return JsonPrimitive(str)
    }

    private fun tryDeserialize(formatter: SimpleDateFormat, dateStr: String): Date? {
        return try {
            formatter.parse(dateStr)
        } catch (e: ParseException) {
            Log.e(this.javaClass.simpleName, "Failed to parse Date due to:", e)
            null
        }
    }

}