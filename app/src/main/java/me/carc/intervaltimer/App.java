package me.carc.intervaltimer;

import android.app.Application;
import android.arch.persistence.room.Room;

import me.carc.intervaltimer.db.AppDatabase;

/**
 * The Application
 * Created by bamptonm on 01-02-2018
 */

public class App extends Application {

    private static final String CARC_DATABASE_NAME = "CarcIntervalTimer.db";

    private AppDatabase mDatabase;

    public synchronized AppDatabase getDB() {
        if (mDatabase == null)
            mDatabase = initDB();
        return mDatabase;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mDatabase = initDB();
    }

    /**
     * Init database
     */
    private AppDatabase initDB() {
        return Room.databaseBuilder(getApplicationContext(), AppDatabase.class, CARC_DATABASE_NAME)
                .fallbackToDestructiveMigration()
                .build();
    }
}
