package me.carc.intervaltimer.ui.activities;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import me.carc.intervaltimer.App;
import me.carc.intervaltimer.R;
import me.carc.intervaltimer.widgets.FeedbackDialog;
import me.carc.intervaltimer.widgets.RatingDialog;

/**
 * Created by bamptonm on 15/02/2018.
 */

public class BaseActivity extends AppCompatActivity {
    private static final String TAG = BaseActivity.class.getName();

    public App getApp() {
        return (App) getApplication();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getApp().setCurrentActivity(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        clearReferences();

    }
    private void clearReferences() {
        AppCompatActivity currActivity = getApp().getCurrentActivity();
        if (this.equals(currActivity))
            getApp().setCurrentActivity(null);
    }

    public void showRatingDialog() {
        final RatingDialog ratingDialog = new RatingDialog.Builder(this)
                .icon(ContextCompat.getDrawable(this, R.mipmap.ic_launcher))
                .session(7)
                .threshold(3)
                .title(getString(R.string.rating_dialog_experience))
                .titleTextColor(R.color.black)
                .positiveButtonText("Not Now")
                .negativeButtonText("Never")
                .positiveBtnTextColor(R.color.white)
                .negativeBtnTextColor(R.color.md_grey_500)
                .formTitle("Submit Feedback")
                .formHint(getString(R.string.rating_dialog_suggestions))
                .formSubmitText("Submit")
                .formCancelText("Cancel")
                .ratingBarColor(R.color.md_yellow_600)
                .onThresholdFailed(new RatingDialog.Builder.RatingThresholdFailedListener() {
                    @Override
                    public void onThresholdFailed(RatingDialog ratingDialog, float rating, boolean thresholdCleared) {
                        emailFeedbackForm(BaseActivity.this);
                        ratingDialog.dismiss();
                    }
                })
                .onRatingChanged(new RatingDialog.Builder.RatingDialogListener() {
                    @Override
                    public void onRatingSelected(float rating, boolean thresholdCleared) {

                    }
                })
                .onRatingBarFormSumbit(new RatingDialog.Builder.RatingDialogFormListener() {
                    @Override
                    public void onFormSubmitted(String feedback) {

                    }
                }).build();

        ratingDialog.show();
    }

    public static void emailFeedbackForm(final Context ctx) {
        FeedbackDialog.Builder builder = new FeedbackDialog.Builder(ctx);
        builder.titleTextColor(R.color.black);

        builder.formTitle(ctx.getString(R.string.shared_string_feedback));
        builder.formHint(ctx.getString(R.string.add_your_comment));
        builder.allowEmpty(false);

        // Positive button
        builder.submitBtnText(ctx.getString(R.string.shared_string_send));
        builder.positiveBtnTextColor(R.color.positiveBtnTextColor);
        builder.positiveBtnBgColor(R.drawable.button_selector_positive);
        builder.onSumbitClick(
                new FeedbackDialog.Builder.FeedbackDialogFormListener() {
                    @Override
                    public void onFormSubmitted(String feedback) {
                        sendFeedBack(ctx, feedback);
                        Toast.makeText(ctx, R.string.shared_string_thankyou, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFormCancel() {
                    }
                });
        builder.build().show();
    }

    private static void sendFeedBack(Context ctx, String text) {
        ShareCompat.IntentBuilder.from((Activity) ctx)
                .setType("message/rfc822")
                .addEmailTo(ctx.getString(R.string.feedback_email))
                .setSubject(ctx.getString(R.string.app_name))
                .setText(text)
                //.setHtmlText(body) //to use HTML in your body text
                .setChooserTitle(ctx.getString(R.string.shared_string_feedback))
                .startChooser();
    }
}
