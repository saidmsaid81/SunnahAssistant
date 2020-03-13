package com.thesunnahrevival.sunnahassistant.data.typeconverters;

import java.util.ArrayList;
import java.util.Arrays;

import androidx.room.TypeConverter;

public class RoomTypeConverter {
    @TypeConverter
    public static String fromArray(ArrayList<String> strings) {

        try {
            StringBuilder stringBuilder = new StringBuilder();
            for (String s : strings) {
                stringBuilder.append(s);
                stringBuilder.append(",");
            }
            return stringBuilder.toString();
        } catch (NullPointerException e) {
            return "";
        }

    }

    @TypeConverter
    public static ArrayList<String> toArray(String concatenatedStrings) {

        return new ArrayList<>(Arrays.asList(concatenatedStrings.split(",")));

    }

}
