package me.carc.intervaltimer.data.local;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.migration.Migration;

/**
 * Applicaiton database (Room database)
 * <p>
 * Created by bamptonm on 04/10/2017.
 */

@Database(entities = {HistoryItem.class}, version = 7)
public abstract class AppDatabase extends RoomDatabase {
    public abstract HistoryDao historyDao();


    public static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE HistoryItem " + " ADD COLUMN locked INTEGER");
        }
    };

    public static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE HistoryItem " + " ADD COLUMN locations TEXT");
        }
    };

    public static final Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE HistoryItem " + " ADD COLUMN distance REAL");
            database.execSQL("ALTER TABLE HistoryItem " + " ADD COLUMN distanceFmt TEXT");
        }
    };

    public static final Migration MIGRATION_5_6 = new Migration(5, 6) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Create the new table
            database.execSQL("CREATE TABLE history_new (elaspedTime TEXT, distanceFmt TEXT, title TEXT, timeRemaining TEXT, roundsCompleted INTEGER NOT NULL, date TEXT, locked INTEGER NOT NULL, locations TEXT, distance REAL NOT NULL, keyID INTEGER NOT NULL, roundsTotal INTEGER NOT NULL, workTime TEXT, restTime TEXT);");
            // Copy the data
            database.execSQL("INSERT INTO history_new (" +
                    "keyID, distanceFmt, elaspedTime, roundsCompleted, timeRemaining, title, locked, date, locations, distance, workTime, workTime, roundsTotal, restTime" +
                    ") SELECT " +
                    "keyID, distanceFmt, elaspedTime, roundsCompleted, timeRemaining, title, locked, date, locations, distance, workTime, workTime, roundsTotal, restTime" +
                    " FROM HistoryItem");
            // Remove the old table
            database.execSQL("DROP TABLE HistoryItem");
            // Change the table name to the correct one
            database.execSQL("ALTER TABLE history_new RENAME TO HistoryItem");
        }
    };
}

