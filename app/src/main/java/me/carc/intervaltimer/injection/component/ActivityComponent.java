package me.carc.intervaltimer.injection.component;

import dagger.Subcomponent;
import me.carc.intervaltimer.injection.PerActivity;
import me.carc.intervaltimer.injection.module.ActivityModule;
import me.carc.intervaltimer.ui.main.MainActivity;

/**
 * This component inject dependencies to all Activities across the application
 */
@PerActivity
@Subcomponent(modules = ActivityModule.class)
public interface ActivityComponent {

    void inject(MainActivity mainActivity);

}
