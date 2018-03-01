package me.carc.intervaltimer.ui.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import me.carc.intervaltimer.ui.fragments.HistoryListDialogFragment;
import me.carc.intervaltimer.utils.Commons;

public class HistoryActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    protected void onResume() {
        super.onResume();
        HistoryListDialogFragment fragment = getHistoryListDialogFragment();
        if(Commons.isNotNull(fragment))
            fragment.show();
        else {
            HistoryListDialogFragment.showInstance(getApplicationContext());
        }

    }

    private HistoryListDialogFragment getHistoryListDialogFragment() {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(HistoryListDialogFragment.ID_TAG);
        return fragment != null && !fragment.isDetached() && !fragment.isRemoving() ? (HistoryListDialogFragment) fragment : null;
    }
}
