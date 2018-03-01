package me.carc.intervaltimer.ui.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.carc.intervaltimer.R;
import me.carc.intervaltimer.model.MainMenuItems;
import me.carc.intervaltimer.widgets.listeners.LocalClickListener;

/**
 * Show other apps from Carc
 * Created by bamptonm on 20/01/2018.
 */

public class MainMenuAdapter extends RecyclerView.Adapter<MainMenuAdapter.ViewHolder> {

    private List<MainMenuItems> mItems;
    private LocalClickListener clickListener;

    public MainMenuAdapter(List<MainMenuItems> items, LocalClickListener listener){
        mItems = items;
        clickListener = listener;
    }
    
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.main_menu_grid_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final MainMenuItems data = mItems.get(holder.getAdapterPosition());

        holder.icon.setImageResource(data.getIconDrawable());
        holder.title.setText(data.getTitleResourceId());
        holder.description.setText(data.getSubTitleResourceId());
        holder.elementHolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickListener.onClick(holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public MainMenuItems getitem(int pos) {
        return mItems.get(pos);
    }


    class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.mainItemIcon)        ImageView icon;
        @BindView(R.id.mainItemTitle)       TextView title;
        @BindView(R.id.mainItemDescription) TextView description;
        @BindView(R.id.mainElementHolder)   LinearLayout elementHolder;

        ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
