package me.carc.intervaltimer.data.local.TypeConverters;

import android.arch.persistence.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

import me.carc.intervaltimer.model.LatLon;

/**
 * Created by bamptonm on 18/02/2018.
 */

public class LatLongTypeConverters {

    @TypeConverter
    public static List<LatLon> stringToSomeObjectList(String data) {
        Gson gson = new Gson();
        if (data == null) {
            return Collections.emptyList();
        }

        Type listType = new TypeToken<List<LatLon>>() {}.getType();

        return gson.fromJson(data, listType);
    }

    @TypeConverter
    public static String someObjectListToString(List<LatLon> someObjects) {
        Gson gson = new Gson();
        return gson.toJson(someObjects);
    }
}