package me.carc.intervaltimer.ui.listeners;

import me.carc.intervaltimer.model.HistoryItem;

/**
 * Created by bamptonm on 05/02/2018.
 */

public interface ClickListener {
    void onClick(HistoryItem item);

    void onLongClick(HistoryItem item);
}