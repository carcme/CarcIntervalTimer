package me.carc.intervaltimer.ui.fragments;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import me.carc.intervaltimer.R;
import me.carc.intervaltimer.data.local.HistoryItem;
import me.carc.intervaltimer.model.LatLon;
import me.carc.intervaltimer.ui.listeners.HistoryListListener;

/**
 * A custom adapter to use with the RecyclerView widget.
 */
public class HistoryListAdapter extends RecyclerView.Adapter<HistoryListAdapter.Holder> {

    private ArrayList<HistoryItem> mItems = new ArrayList<>();
    private HistoryListListener onClickListener;

    public HistoryListAdapter(HistoryListListener listener) {
        onClickListener = listener;
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.history_list_item_layout, viewGroup, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(final Holder holder, int pos) {

        HistoryItem item = mItems.get(pos);

        holder.name.setText(TextUtils.isEmpty(item.getTitle()) ? "<Untitled>" : item.getTitle());
        holder.desc.setText(item.getDate());

        List<LatLon> latLons = item.getLocations();
        if (latLons.size() > 1) {
            String lat = String.valueOf(latLons.get(0).getLatitude());
            String lng = String.valueOf(latLons.get(0).getLongitude());
            String url = "http://maps.google.com/maps/api/staticmap?center=" + lat + "," + lng + "&zoom=14&size=200x200&sensor=false";
            Glide.with(holder.icon.getContext())
                    .load(url)
                    .into(holder.icon);
        } else
            holder.icon.setImageResource(R.drawable.ic_no_gps);

        holder.time.setText(item.getElaspedTime());
        holder.distance.setText(item.getDistanceFmt());

        if (item.isLocked())
            holder.more.setVisibility(View.VISIBLE);
        else
            holder.more.setVisibility(View.GONE);

        /* IMAGE CLICKED*/
        holder.icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickListener.onClickImage(mItems.get(holder.getAdapterPosition()));
            }
        });

        /* VIEW CLICKED*/
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickListener.onClick(mItems.get(holder.getAdapterPosition()));
            }
        });

        /* VIEW LONG CLICKED*/
        holder.mView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                onClickListener.onLongTouch(mItems.get(holder.getAdapterPosition()));
                return true;
            }
        });

        /* OVERFLOW CLICKED*/
        holder.more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickListener.onClickOverflow(v, holder.getAdapterPosition());
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


    public void addItems(List<HistoryItem> items) {
        mItems.addAll(items);
        notifyDataSetChanged();
    }

    public void updateItem(HistoryItem historyItem) {
        for (int i = 0; i < mItems.size(); i++) {
            if(mItems.get(i).getKeyID() == historyItem.getKeyID()) {
                mItems.set(i, historyItem);
                notifyItemChanged(i);
                break;
            }
        }
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


    /**
     * View Holder
     */
    static class Holder extends RecyclerView.ViewHolder {

        View mView;

        ImageView icon;
        TextView name;
        TextView desc;
        TextView time;
        TextView distance;

        ImageView more;

        private Holder(View itemView) {
            super(itemView);
            mView = itemView;

            this.icon = itemView.findViewById(R.id.historyListIcon);
            this.name = itemView.findViewById(R.id.historyListName);
            this.desc = itemView.findViewById(R.id.historyListDesc);

            this.time = itemView.findViewById(R.id.historyListTime);
            this.distance = itemView.findViewById(R.id.historyListDistance);

            more = itemView.findViewById(R.id.historyListMore);
        }
    }
}
