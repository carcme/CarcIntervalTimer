package me.carc.intervaltimer.ui.main;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.reactivestreams.Subscription;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.carc.intervaltimer.R;
import me.carc.intervaltimer.ui.base.MvpBaseActivity;

/**
 * Created by Carc.me on 03.08.16.
 * <p/>
 * Display a menu screen at start up
 */
public class MainActivity extends MvpBaseActivity implements MainMvpView {

    private static final String TAG = MainActivity.class.getName();

    @Inject
    MainPresenter mPresenter;

    private Subscription timerSubscription = null;




    @BindView(R.id.elapsedTime)         TextView elapsedTime;
    @BindView(R.id.timerBackground)     RelativeLayout timerBackground;
    @BindView(R.id.workPreviewText)     TextView workPreviewText;
    @BindView(R.id.restPreviewText)     TextView restPreviewText;
    @BindView(R.id.time_panel)          LinearLayout timeLeftPanel;
    @BindView(R.id.timerRemaining)  TextView textViewTime;
    @BindView(R.id.timerMessage)        TextView timerMessage;
    @BindView(R.id.round_number)        TextView textViewRounds;
    @BindView(R.id.fabSettings)         FloatingActionButton fabSettings;
    @BindView(R.id.fabTimer)            FloatingActionButton fabTimer;
    @BindView(R.id.resetLayer)          RelativeLayout resetLayer;
    @BindView(R.id.resetBtn)            Button resetBtn;
    @BindView(R.id.recyclerView)        RecyclerView recyclerView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityComponent().inject(this);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mPresenter.attachView(this);

        initValues();

    }


    private void initValues() {

        // set reverse layout for the items
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setReverseLayout(true);
        mLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(mLayoutManager);

    }


    @OnClick(R.id.fabTimer)
    void timerFab() {

    }




    /***** MVP View methods implementation *****/


}
