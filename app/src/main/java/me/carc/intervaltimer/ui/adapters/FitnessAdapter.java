package me.carc.intervaltimer.ui.adapters;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.carc.intervaltimer.R;
import me.carc.intervaltimer.model.FitnessExercises;
import me.carc.intervaltimer.widgets.listeners.LocalClickListener;

/**
 * Show other apps from Carc
 * Created by bamptonm on 20/01/2018.
 */

public class FitnessAdapter extends RecyclerView.Adapter<FitnessAdapter.ViewHolder> {

    private List<FitnessExercises> mItems;
    private LocalClickListener clickListener;

    public FitnessAdapter(List<FitnessExercises> items, LocalClickListener listener){
        mItems = items;
        clickListener = listener;
    }
    
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.carc_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final FitnessExercises data = mItems.get(holder.getAdapterPosition());

        holder.image.setImageResource(data.getIconDrawable());
        holder.title.setText(data.getTitle());
        holder.desc.setText(data.getDesc());
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

    public FitnessExercises getitem(int pos) {
        return mItems.get(pos);
    }


    class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.appImage) ImageView image;
        @BindView(R.id.appTitle) TextView title;
        @BindView(R.id.appDesc)  TextView desc;
        @BindView(R.id.card_view)
        CardView elementHolder;

        ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
