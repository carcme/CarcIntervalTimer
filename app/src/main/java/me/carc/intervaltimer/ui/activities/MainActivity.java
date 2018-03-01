package me.carc.intervaltimer.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.LinkedList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.carc.intervaltimer.R;
import me.carc.intervaltimer.data.local.prefs.Preferences;
import me.carc.intervaltimer.model.MainMenuItems;
import me.carc.intervaltimer.ui.adapters.MainMenuAdapter;
import me.carc.intervaltimer.utils.Commons;
import me.carc.intervaltimer.utils.MapUtils;
import me.carc.intervaltimer.widgets.AutoResizeTextView;
import me.carc.intervaltimer.widgets.listeners.LocalClickListener;

public class MainActivity extends BaseActivity {
    
    @BindView(R.id.mainRecyclerview)
    RecyclerView mRecyclerView;

//    @BindView(R.id.summaryFrame)   RelativeLayout summaryFrame;
    @BindView(R.id.summaryDistance) AutoResizeTextView summaryDistance;
    @BindView(R.id.summaryTime)     AutoResizeTextView summaryTime;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        // TODO: 01/03/2018 remove this at some point - either have the front page or ditch it

        boolean REMOVE_LATER = true;

        if(REMOVE_LATER) {

            finish();
            startActivity(new Intent(this, ServicedActivity.class));

        } else {

            MainMenuAdapter adapter = new MainMenuAdapter(buildMenuItems(), new LocalClickListener() {
                @Override
                public void onClick(int pos) {
                    Class activity = ((MainMenuAdapter) mRecyclerView.getAdapter()).getitem(pos).getLauncher();
                    startActivity(new Intent(MainActivity.this, activity));
                }

                @Override
                public void onLongClick(int pos) {
                    ((MainMenuAdapter) mRecyclerView.getAdapter()).getitem(pos);
                }
            });
            mRecyclerView.setAdapter(adapter);
            mRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));

            summaryDistance.setText(MapUtils.getFormattedDistance(Preferences.getTotalDistance(this)));

            long savedTime = Preferences.getTotalTime(this);
            if (savedTime == 0)
                summaryTime.setText(R.string.time_empty);
            else
                summaryTime.setText(Commons.formatTimeString(savedTime));
        }
    }

    private List<MainMenuItems> buildMenuItems() {
        List<MainMenuItems> items = new LinkedList<>();
        items.add(MainMenuItems.TIMER);
        items.add(MainMenuItems.FITNESS);
        items.add(MainMenuItems.HISTORY);

        return items;
    }
}
