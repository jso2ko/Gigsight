package com.jso.gigsight.adapter;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.jso.gigsight.R;
import com.jso.gigsight.databinding.ListItemWidgetBinding;
import com.jso.gigsight.model.Concert;

import java.util.List;

import static com.jso.gigsight.utils.Constants.DEFAULT_IMAGE_SIZE;
import static com.jso.gigsight.utils.Constants.MINI_IMAGE_SIZE;
import static com.jso.gigsight.utils.Constants.NULL_STRING;

public class WidgetAdapter extends RecyclerView.Adapter<WidgetAdapter.WidgetHolder> {

    private Context context;
    private List<Concert> concerts;
    private WidgetClickHandler clickHandler;

    public interface WidgetClickHandler {
        void onClick(Concert concert);
    }

    public WidgetAdapter(Context context, List<Concert> concerts, WidgetClickHandler clickHandler) {
        this.context = context;
        this.concerts = concerts;
        this.clickHandler = clickHandler;
    }

    @NonNull
    @Override
    public WidgetHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        ListItemWidgetBinding binding = DataBindingUtil.inflate
                (inflater, R.layout.list_item_widget, parent, false);
        return new WidgetHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull WidgetHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        if (concerts == null) return 0;
        return concerts.size();
    }

    public void appendConcerts(List<Concert> concertsToAppend) {
        concerts.addAll(concertsToAppend);
        notifyDataSetChanged();
    }

    public void clearConcerts() {
        concerts.clear();
        notifyDataSetChanged();
    }

    class WidgetHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ListItemWidgetBinding binding;

        WidgetHolder(ListItemWidgetBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            binding.getRoot().setOnClickListener(this);
        }

        void bind(int position) {
            binding.widgetTitleTextView.setText(concerts.get(position).getTitle());

            String image = concerts.get(position).getPerformers().get(0).getImage();
            if (image == null || image.equals(NULL_STRING)) {
                binding.widgetThumbnailImageView.setImageResource(R.drawable.square);
            } else {
                image = image.replace(DEFAULT_IMAGE_SIZE, MINI_IMAGE_SIZE);
                Glide.with(context)
                        .load(image)
                        .apply(RequestOptions.placeholderOf(R.drawable.loading)
                                .error(R.drawable.error))
                        .into(binding.widgetThumbnailImageView);
            }
        }

        @Override
        public void onClick(View v) {
            clickHandler.onClick(concerts.get(getAdapterPosition()));
        }
    }
}
