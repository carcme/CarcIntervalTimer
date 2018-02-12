package me.carc.intervaltimer.db;


import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.migration.Migration;

import me.carc.intervaltimer.model.HistoryItem;

/**
 * Applicaiton database (Room database)
 *
 * Created by bamptonm on 04/10/2017.
 */

@Database(entities = {HistoryItem.class}, version = 3)
public abstract class AppDatabase extends RoomDatabase {
    public abstract HistoryDao historyDao();


    public static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE HistoryItem "
                    + " ADD COLUMN locked INTEGER");
        }
    };
}



