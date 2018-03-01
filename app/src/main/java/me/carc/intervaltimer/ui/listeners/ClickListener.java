package me.carc.intervaltimer.ui.listeners;

import me.carc.intervaltimer.data.local.HistoryItem;

/**
 * Created by bamptonm on 05/02/2018.
 */

public interface ClickListener {
    void onClick(int pos);

    void onLongClick(HistoryItem item);
}