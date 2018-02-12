package me.carc.intervaltimer;

import android.app.Application;
import android.arch.persistence.room.Room;
import android.content.Context;

import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;
import me.carc.intervaltimer.db.AppDatabase;
import me.carc.intervaltimer.injection.component.ApplicationComponent;
import me.carc.intervaltimer.injection.component.DaggerApplicationComponent;
import me.carc.intervaltimer.injection.module.ApplicationModule;
import me.carc.intervaltimer.sound.SoundServices;

/**
 * The Application
 * Created by bamptonm on 01-02-2018
 */

public class App extends Application {

    private static final String CARC_DATABASE_NAME = "CarcIntervalTimer.db";

    private ApplicationComponent mApplicationComponent;
    private AppDatabase mDatabase;
    private SoundServices mSoundServices;
    private boolean isActive;


    public static App get(Context context) {
        return (App) context.getApplicationContext();
    }

    public boolean isActive() {
        return isActive;
    }

    public SoundServices getSoundServices() {
        return mSoundServices;
    }

    public void setActive(boolean active) {
        isActive = active;
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

        mSoundServices = new SoundServices(this);

    }

    /**
     * Init database
     */
    private AppDatabase initDB() {
        return Room.databaseBuilder(getApplicationContext(), AppDatabase.class, CARC_DATABASE_NAME)
                .addMigrations(AppDatabase.MIGRATION_2_3)
                .build();
    }

    public ApplicationComponent getComponent() {
        if (mApplicationComponent == null) {
            mApplicationComponent = DaggerApplicationComponent.builder()
                    .applicationModule(new ApplicationModule(this))
                    .build();
        }
        return mApplicationComponent;
    }
}
