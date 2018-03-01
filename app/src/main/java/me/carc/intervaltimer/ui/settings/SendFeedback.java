package me.carc.intervaltimer.ui.settings;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import me.carc.intervaltimer.R;
import me.carc.intervaltimer.utils.IntentUtils;
import me.carc.intervaltimer.widgets.FeedbackDialog;
import me.carc.intervaltimer.widgets.RatingDialog;

/**
 * Holder for sending feedback info to Firebase
 * Created by bamptonm on 29/10/2017.
 */

public class SendFeedback {
    private static final String TAG = SendFeedback.class.getName();

    public static final int TYPE_FEEDBACK = 0;
    public static final int TYPE_RATE = 1;


    public SendFeedback(Context ctx, int type) {
        if(type == TYPE_FEEDBACK) {
            feedback(ctx);
        }  else if(type == TYPE_RATE) {
            rate(ctx);
        }
    }

    private void feedback(final Context ctx) {

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

    private void rate(final Context ctx) {
        final RatingDialog ratingDialog = new RatingDialog.Builder(ctx)
                .icon(ContextCompat.getDrawable(ctx, R.mipmap.ic_launcher))
                .threshold(3)
                .title(ctx.getString(R.string.ratings_request_title))
                .titleTextColor(R.color.black)
                .formTitle(ctx.getString(R.string.feedback_request_title))
                .formHint(ctx.getString(R.string.feedback_request_hint))
                .formSubmitText(ctx.getString(R.string.rating_dialog_submit))
                .formCancelText(ctx.getString(R.string.rating_dialog_cancel))
                .ratingBarColor(R.color.colorAccent)
                .positiveBtnTextColor(R.color.positiveBtnTextColor)
                .positiveBtnBackgroundColor(R.drawable.button_selector_positive)

                .negativeBtnTextColor(R.color.negativeBtnTextColorPale)
                .negativeBtnBackgroundColor(R.drawable.button_selector_negative)
                .onThresholdCleared(new RatingDialog.Builder.RatingThresholdClearedListener() {
                    @Override
                    public void onThresholdCleared(RatingDialog dlg, float rating, boolean thresholdCleared) {

                        try {
                            ctx.startActivity(IntentUtils.openPlayStore(ctx));
                        } catch (ActivityNotFoundException ex) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
                            builder.setTitle(R.string.shared_string_error);
                            builder.setMessage(R.string.error_playstore_not_found);
                            dlg.show();
                        }
                        dlg.dismiss();
                    }
                })

                .onRatingBarFormSumbit(new RatingDialog.Builder.RatingDialogFormListener() {
                    @Override
                    public void onFormSubmitted(String feedback) {
                        sendFeedBack(ctx, feedback);
                        Toast.makeText(ctx, R.string.shared_string_thankyou, Toast.LENGTH_SHORT).show();
                    }
                }).build();

        ratingDialog.show();
    }


    private static void sendFeedBack(Context ctx, String text) {
        ShareCompat.IntentBuilder.from((Activity) ctx)
                .setType("message/rfc822")
                .addEmailTo(ctx.getString(R.string.feedback_email))
                .setSubject(ctx.getString(R.string.app_name))
                .setText(text)
                //.setHtmlText(body) //If you are using HTML in your body text
                .setChooserTitle(ctx.getString(R.string.shared_string_feedback))
                .startChooser();
/*
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/rfc822");
        intent.putExtra(Intent.EXTRA_EMAIL, ctx.getString(R.string.feedback_email));
        intent.putExtra(Intent.EXTRA_SUBJECT, ctx.getString(R.string.app_name));
        intent.putExtra(Intent.EXTRA_TEXT, "I'm email body.");

        ctx.startActivity(Intent.createChooser(intent, "Send Email"));

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setAction(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_SUBJECT, ctx.getString(R.string.app_name));
        intent.putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=" + ctx.getPackageName());
        intent.setType("text/plain");
        ctx.startActivity(Intent.createChooser(intent, ctx.getString(R.string.shared_string_share)));
*/
    }
}
