package me.carc.intervaltimer.widgets;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import me.carc.intervaltimer.R;
import me.carc.intervaltimer.model.HistoryItem;
import me.carc.intervaltimer.widgets.listeners.HistoryItemLockListener;

/**
 * V.Simple viewer for the history item
 * Created by bamptonm on 11/02/2018.
 */

public class HistoryViewerDialog extends Dialog {

    private final String TAG = HistoryViewerDialog.class.getName();

    private final HistoryItemLockListener lockListener;

    public HistoryViewerDialog(@NonNull Context context, final HistoryItem item, HistoryItemLockListener listener) {
        super(context);
        setContentView(R.layout.history_viewer_dialog);

        this.lockListener = listener;

        Button setBtn = findViewById(R.id.setBtn);
        Button cancelBtn = findViewById(R.id.cancelBtn);

        int timeDelimit = item.getDate().lastIndexOf(":");
        String date = item.getDate().substring(0, timeDelimit);
        String time = item.getDate().substring(timeDelimit + 1, item.getDate().length());

        TextView dateTV      = findViewById(R.id.date);
        TextView timeTV      = findViewById(R.id.time);
        TextView durationTV  = findViewById(R.id.duration);
        TextView roundsTV    = findViewById(R.id.rounds);
        TextView workTV      = findViewById(R.id.work);
        TextView restTV      = findViewById(R.id.rest);
        final ImageButton itemLock = findViewById(R.id.itemLock);

        dateTV.setText(date);
        timeTV.setText(time);
        durationTV.setText(item.getElaspedTime());
        roundsTV.setText(String.format(context.getString(R.string.roundsOf), item.getRoundsCompleted(), item.getRoundsTotal()));
        workTV.setText(item.getWorkTime());
        restTV.setText(item.getRestTime());


        /* OK button click listener  */
        setBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        /* Cancel button click listener  */
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        cancelBtn.setVisibility(View.GONE);


        if(item.isLocked())
            itemLock.setImageResource(R.drawable.ic_locked);
        else
            itemLock.setImageResource(R.drawable.ic_unlocked);

        itemLock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(item.isLocked()) {
                    lockListener.onLockItme(false);
                    item.setLocked(false);
                    itemLock.setImageResource(R.drawable.ic_unlocked);
                } else {
                    lockListener.onLockItme(true);
                    item.setLocked(true);
                    itemLock.setImageResource(R.drawable.ic_locked);
                }
            }
        });
    }
}
