package com.thesunnahrevival.sunnahassistant.data.typeconverters

import android.net.Uri
import androidx.room.TypeConverter
import com.thesunnahrevival.sunnahassistant.data.model.Frequency
import java.lang.Integer.parseInt
import java.util.*
import kotlin.collections.ArrayList

class RoomTypeConverter {
    @TypeConverter
    fun fromArray(numbers: ArrayList<Int?>): String {
        return try {
            val stringBuilder = StringBuilder()
            for (s in numbers) {
                stringBuilder.append(s.toString())
                stringBuilder.append(",")

            }
            stringBuilder.toString()
        } catch (e: NullPointerException) {
            ""
        }
    }

    @TypeConverter
    fun toArray(concatenatedStrings: String): ArrayList<Int> {
        val list = ArrayList<Int>()
        val array = concatenatedStrings.split(",").toTypedArray()
        try{
            for (index in 0..array.size)
                list.add(parseInt(array[index]))
        }
        catch(exception: NumberFormatException){
            return list
        }
        return list
    }

    
    @TypeConverter
    fun fromUri(uri: Uri?): String {
        return uri?.toString() ?: ""
    }

    
    @TypeConverter
    fun toUri(stringUri: String?): Uri {
        return Uri.parse(stringUri)
    }

    
    @TypeConverter
    fun fromTreeSet(strings: TreeSet<String?>): String {
        return try {
            val stringBuilder = StringBuilder()
            for (s in strings) {
                stringBuilder.append(s)
                stringBuilder.append(",")
            }
            stringBuilder.toString()
        } catch (e: NullPointerException) {
            ""
        }
    }

    
    @TypeConverter
    fun toTreeSet(concatenatedStrings: String): TreeSet<String> {
        val list = listOf(*concatenatedStrings.split(",").toTypedArray())
        return TreeSet(list.filter { it.isNotBlank() })
    }

    @TypeConverter
    fun fromFrequency(frequency: Frequency): String {
        return frequency.ordinal.toString()
    }

    @TypeConverter
    fun toFrequency(number: String): Frequency {
        return try {
            Frequency.values()[parseInt(number)]
        }
        catch(exception: NumberFormatException) {
            Frequency.Daily
        }
    }
}