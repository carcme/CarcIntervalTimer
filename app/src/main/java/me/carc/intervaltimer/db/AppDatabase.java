package me.carc.intervaltimer.db;


import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

import me.carc.intervaltimer.model.HistoryItem;

/**
 * Applicaiton database (Room database)
 *
 * Created by bamptonm on 04/10/2017.
 */

@Database(entities = {HistoryItem.class}, version = 2)
public abstract class AppDatabase extends RoomDatabase {
    public abstract HistoryDao historyDao();
}