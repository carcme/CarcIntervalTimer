package me.carc.intervaltimer.ui.listeners;

import android.view.View;

import me.carc.intervaltimer.data.local.HistoryItem;

public interface HistoryListListener {
    void onClick(HistoryItem historyItem);

    void onLongTouch(HistoryItem historyItem);

    void onClickImage(HistoryItem historyItem);

    void onClickOverflow(View v, int position);
}
