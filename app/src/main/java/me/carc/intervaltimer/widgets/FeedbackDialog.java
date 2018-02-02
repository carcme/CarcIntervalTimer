package me.carc.intervaltimer.widgets;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatDialog;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.TextView;

import me.carc.intervaltimer.R;

/**
 * Show a rotating compass dialog page
 * Created by bamptonm on 6/3/17.
 */

public class FeedbackDialog extends AppCompatDialog implements View.OnClickListener {

    private Context context;
    private Builder builder;
    private TextView titleTv, submitBtn, cancelBtn;
    private EditText feedbackText;


    private FeedbackDialog(Context context, Builder builder) {
        super(context);

        getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;

        this.context = context;
        this.builder = builder;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        assert getWindow() != null;
        getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        setContentView(R.layout.dialog_feedback);

        titleTv = findViewById(R.id.dlg_feedback_title);
        submitBtn = findViewById(R.id.dlg_btn_feedback_submit);
        cancelBtn = findViewById(R.id.dlg_btn_feedback_cancel);
        feedbackText = findViewById(R.id.dlg_feedback_edit);

        init();
    }


    private void init() {

        titleTv.setText(builder.formTitle);
        submitBtn.setText(builder.submitBtnText);
        cancelBtn.setText(builder.cancelBtnText);
        feedbackText.setText(builder.formText);
        feedbackText.setHint(builder.formHint);

        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.colorAccent, typedValue, true);
        int color = typedValue.data;

        titleTv.setTextColor(builder.titleTextColor != 0 ? ContextCompat.getColor(context, builder.titleTextColor) : ContextCompat.getColor(context, R.color.black));
        submitBtn.setTextColor(builder.positiveTextColor != 0 ? ContextCompat.getColor(context, builder.positiveTextColor) : color);
        cancelBtn.setTextColor(builder.negativeTextColor != 0 ? ContextCompat.getColor(context, builder.negativeTextColor) : ContextCompat.getColor(context, R.color.md_grey_500));

        if (builder.formTextColor != 0) {
            feedbackText.setTextColor(ContextCompat.getColor(context, builder.formTextColor));
        }

        if (builder.positiveBackgroundColor != 0) {
            submitBtn.setBackgroundResource(builder.positiveBackgroundColor);

        }
        if (builder.negativeBackgroundColor != 0) {
            cancelBtn.setBackgroundResource(builder.negativeBackgroundColor);
        }

        submitBtn.setOnClickListener(this);
        cancelBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.dlg_btn_feedback_submit) {

            String feedback = feedbackText.getText().toString().trim();
            if (!builder.allowEmpty && TextUtils.isEmpty(feedback)) {

                Animation shake = AnimationUtils.loadAnimation(context, R.anim.shake);
                feedbackText.startAnimation(shake);
                return;
            }

            if (builder.feedbackDialogFormListener != null) {
                builder.feedbackDialogFormListener.onFormSubmitted(feedback);
            }

            dismiss();

        } else if (view.getId() == R.id.dlg_btn_feedback_cancel) {

            if (builder.feedbackDialogFormListener != null) {
                builder.feedbackDialogFormListener.onFormCancel();
            }

            dismiss();
        }
    }

    public TextView getFormTitleTextView() {
        return titleTv;
    }

    public TextView getFormSumbitTextView() {
        return submitBtn;
    }

    public TextView getFormCancelTextView() {
        return cancelBtn;
    }

    @Override
    public void show() {

        super.show();
    }

    public static class Builder {

        private final Context context;
        private String formTitle, submitBtnText, cancelBtnText, formHint, formText;
        private boolean allowEmpty;
        private int positiveTextColor, negativeTextColor, titleTextColor, formTextColor;
        private int positiveBackgroundColor, negativeBackgroundColor;
        private FeedbackDialogFormListener feedbackDialogFormListener;


        public interface FeedbackDialogFormListener {
            void onFormSubmitted(String feedback);
            void onFormCancel();
        }

        public Builder(Context context) {
            this.context = context;
            initText();
        }

        private void initText() {
            formTitle = context.getString(R.string.rating_dialog_feedback_title);
            submitBtnText = context.getString(R.string.rating_dialog_submit);
            cancelBtnText = context.getString(R.string.rating_dialog_cancel);
            formHint = context.getString(R.string.rating_dialog_suggestions);
        }

        public Builder titleTextColor(int titleTextColor) {
            this.titleTextColor = titleTextColor;
            return this;
        }

        public Builder positiveBtnTextColor(int positiveTextColor) {
            this.positiveTextColor = positiveTextColor;
            return this;
        }

        public Builder negativeBtnTextColor(int negativeTextColor) {
            this.negativeTextColor = negativeTextColor;
            return this;
        }

        public Builder positiveBtnBgColor(int positiveBackgroundColor) {
            this.positiveBackgroundColor = positiveBackgroundColor;
            return this;
        }

        public Builder negativeBtnBgColor(int negativeBackgroundColor) {
            this.negativeBackgroundColor = negativeBackgroundColor;
            return this;
        }

        public FeedbackDialog.Builder onSumbitClick(FeedbackDialog.Builder.FeedbackDialogFormListener fbDlgFormListener) {
            this.feedbackDialogFormListener = fbDlgFormListener;
            return this;
        }

        public Builder formTitle(String formTitle) {
            this.formTitle = formTitle;
            return this;
        }

        public Builder formHint(String formHint) {
            this.formHint = formHint;
            return this;
        }

        public Builder formText(String formHint) {
            this.formText = formHint;
            return this;
        }

        public Builder submitBtnText(String submitText) {
            this.submitBtnText = submitText;
            return this;
        }

        public Builder allowEmpty(boolean allowEmpty) {
            this.allowEmpty = allowEmpty;
            return this;
        }


        public Builder cancelBtnText(String cancelText) {
            this.cancelBtnText = cancelText;
            return this;
        }


        public Builder formTextColor(int feedBackTextColor) {
            this.formTextColor = feedBackTextColor;
            return this;
        }

        public FeedbackDialog build() {
            return new FeedbackDialog(context, this);
        }
    }
}
