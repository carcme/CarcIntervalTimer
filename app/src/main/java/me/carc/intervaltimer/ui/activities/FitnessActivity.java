package me.carc.intervaltimer.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.LinkedList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.carc.intervaltimer.R;
import me.carc.intervaltimer.model.FitnessExercises;
import me.carc.intervaltimer.ui.adapters.FitnessAdapter;
import me.carc.intervaltimer.widgets.listeners.LocalClickListener;

public class FitnessActivity extends AppCompatActivity {

    @BindView(R.id.recycler_view)   RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fitness);
        ButterKnife.bind(this);

        FitnessAdapter adapter = new FitnessAdapter(buildMenuItems(), new LocalClickListener() {
            @Override
            public void onClick(int pos) {
                FitnessExercises exercise = ((FitnessAdapter)mRecyclerView.getAdapter()).getitem(pos);
                Intent intent = new Intent(FitnessActivity.this, FitnessViewerActivity.class);

                intent.putExtra(FitnessViewerActivity.EXTRA_EXERCISE_INDEX, exercise);

                startActivity(intent);
            }

            @Override
            public void onLongClick(int pos) {
                ((FitnessAdapter)mRecyclerView.getAdapter()).getitem(pos);
            }
        });
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private List<FitnessExercises> buildMenuItems() {
        List<FitnessExercises> items = new LinkedList<>();
        items.add(FitnessExercises.BURPEE);
        items.add(FitnessExercises.JUMPJACK);
        items.add(FitnessExercises.PLANK);
        items.add(FitnessExercises.SITUP);
        items.add(FitnessExercises.DIPS);
        items.add(FitnessExercises.PUSHUP);
        return items;
    }

}
