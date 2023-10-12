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
import com.jso.gigsight.databinding.ListItemConcertBinding;
import com.jso.gigsight.model.Concert;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.jso.gigsight.utils.Constants.DATE_INPUT;
import static com.jso.gigsight.utils.Constants.DEFAULT_IMAGE_SIZE;
import static com.jso.gigsight.utils.Constants.ITEM_DATE_OUTPUT;
import static com.jso.gigsight.utils.Constants.MINI_IMAGE_SIZE;
import static com.jso.gigsight.utils.Constants.NULL_STRING;
import static com.jso.gigsight.utils.Constants.TARGET_REPLACE;

public class ConcertAdapter extends RecyclerView.Adapter<ConcertAdapter.ConcertHolder> {

    private Context context;
    private List<Concert> concerts;
    private ConcertClickHandler clickHandler;

    public interface ConcertClickHandler {
        void onClick(Concert concert);
    }

    public ConcertAdapter(Context context, List<Concert> concerts, ConcertClickHandler clickHandler) {
        this.context = context;
        this.concerts = concerts;
        this.clickHandler = clickHandler;
    }

    @NonNull
    @Override
    public ConcertHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        ListItemConcertBinding binding = DataBindingUtil.inflate
                (inflater, R.layout.list_item_concert, parent, false);
        return new ConcertHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ConcertHolder holder, int position) {
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

    class ConcertHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ListItemConcertBinding binding;


        ConcertHolder(ListItemConcertBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            binding.getRoot().setOnClickListener(this);
        }

        void bind(int position) {
            binding.titleTextView.setText(concerts.get(position).getTitle());

            if (concerts.get(position).getPerformers() != null) {
                for (int i = 0; i < concerts.get(position).getPerformers().size(); i++) {
                    String image = concerts.get(position).getPerformers().get(i).getImage();
                    if (image == null || image.equals(NULL_STRING)) {
                        binding.thumbnailImageView.setImageResource(R.drawable.square);
                    } else {
                        image = image.replace(DEFAULT_IMAGE_SIZE, MINI_IMAGE_SIZE);
                        Glide.with(context)
                                .load(image)
                                .apply(RequestOptions.placeholderOf(R.drawable.loading)
                                        .error(R.drawable.error))
                                .into(binding.thumbnailImageView);
                    }
                }
            }

            try {
                String dateTimeLocal = concerts.get(position).getDateTime().replace(TARGET_REPLACE, " ");
                DateFormat dateInput = new SimpleDateFormat(DATE_INPUT, Locale.getDefault());
                DateFormat dateOutput = new SimpleDateFormat(ITEM_DATE_OUTPUT, Locale.getDefault());

                Date dateObject = dateInput.parse(dateTimeLocal);
                String formattedDate = dateOutput.format(dateObject);
                binding.dateTextView.setText(formattedDate);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onClick(View v) {
            clickHandler.onClick(concerts.get(getAdapterPosition()));
        }
    }
}
