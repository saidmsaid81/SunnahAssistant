package com.thesunnahrevival.common.data.typeconverters

import android.net.Uri
import androidx.room.TypeConverter
import com.batoulapps.adhan.CalculationMethod
import com.batoulapps.adhan.Madhab
import com.thesunnahrevival.common.data.model.Frequency
import java.util.*

class RoomTypeConverter {
    @TypeConverter
    fun fromTreeSetOfNumbers(numbers: TreeSet<Int>?): String {
        return numbers?.joinToString(separator = ",") ?: ""
    }

    @TypeConverter
    fun toTreeSetOfNumbers(concatenatedNumbersString: String?): TreeSet<Int> {
        val treeSet = TreeSet<Int>()
        concatenatedNumbersString?.split(",")?.forEach { string ->
            string.toIntOrNull()?.let {
                treeSet.add(it)
            }
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
        return strings.joinToString(separator = ",")
    }

    @TypeConverter
    fun toTreeSet(concatenatedStrings: String): TreeSet<String> {
        val treeSet = TreeSet<String>()
        treeSet.addAll(concatenatedStrings.split(","))
        treeSet.remove("")
        return treeSet
    }

    @TypeConverter
    fun fromFrequency(frequency: Frequency): String {
        return frequency.ordinal.toString()
    }

    @TypeConverter
    fun toFrequency(numberString: String): Frequency {
        val number = numberString.toIntOrNull() ?: Frequency.Daily.ordinal
        return Frequency.values().getOrElse(number) { Frequency.Daily }
    }

    @TypeConverter
    fun toCalculationMethod(number: Int): CalculationMethod {
        return CalculationMethod.values()
            .getOrElse(number) { CalculationMethod.MUSLIM_WORLD_LEAGUE }
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

    @TypeConverter
    fun fromDate(date: Date) = date.time

    @TypeConverter
    fun toDate(dateInMilliseconds: Long) = Date(dateInMilliseconds)

    @TypeConverter
    fun fromBooleanArray(array: BooleanArray): String {
        return array.joinToString(",")
    }

    @TypeConverter
    fun toBooleanArray(string: String): BooleanArray {
        val booleanArray = BooleanArray(5) { true }
        if (string.isNotBlank()) {
            val array = string.split(",")
            booleanArray.forEachIndexed { index, _ ->
                booleanArray[index] = array.getOrElse(index) { "true" } == "true"
            }
        }

        return booleanArray
    }

    @TypeConverter
    fun fromIntArray(array: IntArray): String {
        return array.joinToString(separator = ",")
    }

    @TypeConverter
    fun toIntArray(string: String): IntArray {
        val intArray = IntArray(5) { 0 }
        if (string.isNotBlank()) {
            val array = string.split(",")
            intArray.forEachIndexed { index, _ ->
                intArray[index] = array.getOrElse(index) { "0" }.toIntOrNull() ?: 0
            }
        }
        return intArray
    }
}