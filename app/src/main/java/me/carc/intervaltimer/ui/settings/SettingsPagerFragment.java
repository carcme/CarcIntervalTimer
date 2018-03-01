package me.carc.intervaltimer.ui.settings;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mikepenz.aboutlibraries.LibsBuilder;

import me.carc.intervaltimer.R;

public class SettingsPagerFragment extends Fragment {

    public static final String TAG_ID = SettingsPagerFragment.class.getName();

    private static final int SETTING_TAB = 0;
    private static final int ABOUT_TAB = 1;
    private static final int CARC_TAB = 2;
    private static final int LIBS_TAB = 3;
    private static final int TAB_COUNT = LIBS_TAB + 1;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings_tabs, container, false);

        final ViewPager viewPager = view.findViewById(R.id.pager);
        viewPager.setOffscreenPageLimit(TAB_COUNT);         // don't recreate the fragments when changing tabs
        viewPager.setAdapter(new AboutPagerAdapter(getFragmentManager()));

        final TabLayout tabLayout = view.findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setTabMode(TabLayout.MODE_FIXED);

        tabLayout.setSelectedTabIndicatorColor(ContextCompat.getColor(getActivity(), R.color.white));

        int color = ContextCompat.getColor(getActivity(), R.color.color_settings);
        tabLayout.setBackgroundColor(color);

        return view;
    }

    private class AboutPagerAdapter extends FragmentPagerAdapter {
        private AboutPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            if (i == ABOUT_TAB) {
                return new AboutFragment();
            } else if (i == SETTING_TAB) {
                return new SettingsTabFragment();
            } else if (i == CARC_TAB) {
                return new CarcFragment();
            } else if (i == LIBS_TAB) {
                return new LibsBuilder()
                        .withAboutIconShown(true)
                        .withAboutVersionShown(true)
                        .withAboutDescription("Icon made by <a href src=\"http://www.freepik.com\">Freepik</a> from <a href src=\"https://www.flaticon.com\">Flaticon</a>")
                        .withFields(R.string.class.getFields())
                        .fragment();
            }
            return new SettingsTabFragment();
        }

        @Override
        public int getCount() {
            return TAB_COUNT;
        }

        @Override
        public CharSequence getPageTitle(int i) {
            if (i == ABOUT_TAB) {
                return getString(R.string.shared_string_about);
            } else if (i == CARC_TAB) {
                return getString(R.string.shared_string_extra);
            } else if (i == LIBS_TAB) {
                return getString(R.string.tab_libraries);
            }
            return getString(R.string.shared_string_settings);
        }
    }
}
