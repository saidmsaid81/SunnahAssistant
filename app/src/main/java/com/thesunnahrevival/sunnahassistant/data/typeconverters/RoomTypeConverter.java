package com.thesunnahrevival.sunnahassistant.data.typeconverters;

import android.net.Uri;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

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

    @TypeConverter
    public static String fromUri(Uri uri){
        if (uri != null)
            return uri.toString();
        return "";
    }

    @TypeConverter
    public static Uri toUri(String stringUri){
        return Uri.parse(stringUri);
    }

    @TypeConverter
    public static String fromHashSet(HashSet<String> strings){
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
    public static HashSet<String> toHashSet(String concatenatedStrings) {

        return new HashSet<>(Arrays.asList(concatenatedStrings.split(",")));

    }

}
