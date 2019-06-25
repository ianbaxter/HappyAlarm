package com.birdbathapps.HappyAlarmClock.database;

import androidx.room.TypeConverter;
import java.util.Arrays;

public class Converters {
    @TypeConverter
    public static boolean[] fromString(String string) {
        String[] parts = string.split(", ");

        boolean[] booleanArray = new boolean[parts.length];
        for (int i = 0; i < booleanArray.length; i++) {
            booleanArray[i] = Boolean.parseBoolean(parts[i]);
        }
        return booleanArray;
    }

    @TypeConverter
    public static String fromBooleanArray(boolean[] booleanArray) {
        String arrayAsString = Arrays.toString(booleanArray);
        arrayAsString = arrayAsString.replace("[", "");
        arrayAsString = arrayAsString.replace("]", "");
        return arrayAsString;
    }
}
