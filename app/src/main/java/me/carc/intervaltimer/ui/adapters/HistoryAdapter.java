package me.carc.intervaltimer.ui.adapters;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.carc.intervaltimer.R;
import me.carc.intervaltimer.model.HistoryItem;
import me.carc.intervaltimer.ui.listeners.ClickListener;

/**
 * Show other apps from Carc
 * Created by bamptonm on 20/01/2018.
 */

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private List<HistoryItem> mItems = new ArrayList<>();
    private ClickListener clickListener;

    public HistoryAdapter(ClickListener listener){
        clickListener = listener;
    }
    
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final HistoryItem data = mItems.get(position);

        Context ctx = holder.historyCardView.getContext();

        holder.historyDate.setText(data.getDate());
        holder.historyElaspedTime.setText(data.getElaspedTime());

        holder.historyRounds.setText(String.format(Locale.US,
                ctx.getString(R.string.history_item_rounds), data.getRoundsCompleted(), data.getRoundsTotal()));

        holder.historyWorkRest.setText(String.format(Locale.US,
                ctx.getString(R.string.history_item_times), data.getWorkTime(), data.getRestTime()));

        holder.historyCardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                clickListener.onLongClick(data);
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public int getItemPosition(HistoryItem item) {
        return mItems.indexOf(item);
    }

    public void addItem(HistoryItem item) {
        mItems.add(item);
        notifyItemChanged(mItems.size()-1);
    }

    public void addItems(List<HistoryItem> items) {
        mItems.addAll(items);
        notifyDataSetChanged();
    }

    public void removeItem(HistoryItem item) {
        mItems.remove(item);
        notifyDataSetChanged();
    }

    public boolean removeItem(int position) {
        if (mItems.size() >= position + 1) {
            mItems.remove(position);
            return true;
        }
        return false;
    }

    public void removeAll() {
        mItems.clear();
        notifyDataSetChanged();
    }


    class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.historyDate)         TextView historyDate;
        @BindView(R.id.historyElaspedTime)  TextView historyElaspedTime;
        @BindView(R.id.historyRounds)       TextView historyRounds;
        @BindView(R.id.historyWorkRest)     TextView historyWorkRest;
        @BindView(R.id.historyCardView)     CardView historyCardView;

        ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
