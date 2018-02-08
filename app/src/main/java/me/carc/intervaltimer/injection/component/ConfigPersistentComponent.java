package me.carc.intervaltimer.injection.component;

import dagger.Component;
import me.carc.intervaltimer.injection.ConfigPersistent;
import me.carc.intervaltimer.injection.module.ActivityModule;

/**
 * A dagger component that will live during the lifecycle of an Activity but it won't
 * be destroy during configuration changes. Check {@link me.carc.intervaltimer.ui.base.MvpBaseActivity} to see how this components
 * survives configuration changes.
 * Use the {@link me.carc.intervaltimer.injection.ConfigPersistent} scope to annotate dependencies that need to survive
 * configuration changes (for example Presenters).
 */
@ConfigPersistent
@Component(dependencies = ApplicationComponent.class)
public interface ConfigPersistentComponent {

    ActivityComponent activityComponent(ActivityModule activityModule);

}