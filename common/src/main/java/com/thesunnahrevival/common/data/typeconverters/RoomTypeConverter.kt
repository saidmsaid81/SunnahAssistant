package com.thesunnahrevival.common.data.typeconverters

import android.net.Uri
import androidx.room.TypeConverter
import com.batoulapps.adhan.CalculationMethod
import com.batoulapps.adhan.Madhab
import com.thesunnahrevival.common.data.model.Frequency
import java.lang.Integer.parseInt
import java.util.*

class RoomTypeConverter {
    @TypeConverter
    fun fromTreeSetOfNumbers(numbers: TreeSet<Int>?): String {
        if (numbers == null)
            return ""
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
    fun toTreeSetOfNumbers(concatenatedStrings: String?): TreeSet<Int> {
        val treeSet = TreeSet<Int>()

        if (concatenatedStrings == null)
            return treeSet

        val array = concatenatedStrings.split(",")
        try {
            for (index in 0..array.size)
                treeSet.add(parseInt(array[index]))
        } catch (exception: NumberFormatException) {
            return treeSet
        }
        return treeSet
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
        } catch (exception: NumberFormatException) {
            Frequency.Daily
        }
    }

    @TypeConverter
    fun toCalculationMethod(number: Int): CalculationMethod {
        return try {
            CalculationMethod.values()[number]
        } catch (exception: ArrayIndexOutOfBoundsException) {
            CalculationMethod.MUSLIM_WORLD_LEAGUE
        }
    }

    @TypeConverter
    fun fromCalculationMethod(calculationMethod: CalculationMethod): Int {
        return calculationMethod.ordinal
    }

    @TypeConverter
    fun toMadhab(number: Int): Madhab {
        return try {
            Madhab.values()[number]
        } catch (exception: ArrayIndexOutOfBoundsException) {
            return Madhab.SHAFI
        }
    }

    @TypeConverter
    fun fromMadhab(madhab: Madhab): Int {
        return madhab.ordinal
    }
}