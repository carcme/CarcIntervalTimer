package me.carc.intervaltimer.data.local.TypeConverters;

import android.arch.persistence.room.TypeConverter;
import java.util.Date;

/**
 * Created by bamptonm on 26/02/2018.
 */

public class DateTypeConverter {

    @TypeConverter
    public static Date toDate(Long value) {
        return value == null ? null : new Date(value);
    }

    @TypeConverter
    public static Long toLong(Date value) {
        return value == null ? null : value.getTime();
    }
}