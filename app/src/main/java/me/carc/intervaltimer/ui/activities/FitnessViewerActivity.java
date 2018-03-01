package me.carc.intervaltimer.ui.activities;

import android.app.ProgressDialog;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.text.Html;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.carc.intervaltimer.R;
import me.carc.intervaltimer.model.FitnessExercises;

public class FitnessViewerActivity extends AppCompatActivity {

    private static final String TAG = FitnessViewerActivity.class.getName();
    public static final String EXTRA_EXERCISE_INDEX = "EXTRA_EXERCISE_INDEX";

    @BindView(R.id.fitViewerIcon) ImageView icon;
    @BindView(R.id.videoViewer) VideoView videoView;

    @BindView(R.id.fitViewerCard) CardView carc;
    @BindView(R.id.fitViewerTitle) TextView title;
    @BindView(R.id.fitViewerDescription) TextView desc;
    @BindView(R.id.fitViewerInstructions) TextView instruction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fitness_viewer);
        ButterKnife.bind(this);

        FitnessExercises exercise = (FitnessExercises) getIntent().getSerializableExtra(EXTRA_EXERCISE_INDEX);

        icon.setImageResource(exercise.getIconDrawable());
        title.setText(exercise.getTitle());
        desc.setText(exercise.getDesc());
        instruction.setText(Html.fromHtml(getString(exercise.getInstruct())));

        playVideo(getString(exercise.getUrl()));
    }

    private void playVideo(String url) {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading...");
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        MediaController mediaControls = new MediaController(this);
        videoView.setMediaController(mediaControls);

//        videoView.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.kitkat));

        if(TextUtils.isEmpty(url))
            videoView.setVideoPath("http://www.ebookfrenzy.com/android_book/movie.mp4");
        else
            videoView.setVideoPath(url);

        videoView.requestFocus();
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                progressDialog.dismiss();
                videoView.start();
                mp.setLooping(true);
            }
        });
    }
}
