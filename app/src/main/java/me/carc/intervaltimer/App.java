package me.carc.intervaltimer;

import android.app.Application;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;

import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;
import me.carc.intervaltimer.data.local.AppDatabase;

/**
 * The Application
 * Created by bamptonm on 01-02-2018
 */

public class App extends Application {

    private static final String CARC_DATABASE_NAME = "CarcIntervalTimer.db";

    private AppDatabase mDatabase;
    private AppCompatActivity mCurrentActivity = null;

    public static App get(Context context) {
        return (App) context.getApplicationContext();
    }

    public AppCompatActivity getCurrentActivity() {
        return mCurrentActivity;
    }
    public void setCurrentActivity(AppCompatActivity mCurrentActivity) {
        this.mCurrentActivity = mCurrentActivity;
    }


    public synchronized AppDatabase getDB() {
        if (mDatabase == null)
            mDatabase = initDB();
        return mDatabase;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if(!BuildConfig.DEBUG)
            Fabric.with(this, new Crashlytics());

        mDatabase = initDB();
    }

    /**
     * Init database
     */
    private AppDatabase initDB() {
        return Room.databaseBuilder(getApplicationContext(), AppDatabase.class, CARC_DATABASE_NAME)
                .fallbackToDestructiveMigration()
/*
                .addMigrations(AppDatabase.MIGRATION_2_3)
                .addMigrations(AppDatabase.MIGRATION_3_4)
                .addMigrations(AppDatabase.MIGRATION_4_5)
                .addMigrations(AppDatabase.MIGRATION_5_6)
*/
                .build();
    }
}
