package pangtourista.project.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.TimeZone;

import pangtourista.project.Models.NewsEvents;
import pangtourista.project.R;
import pangtourista.project.activities.NewsEventsDetailActivity;
import pangtourista.project.databinding.ItemNewsEventsBinding;

public class ProvincialNewsEventsAdapter extends  RecyclerView.Adapter<ProvincialNewsEventsAdapter.NewsEventViewHolder>{

    Context context;
    ArrayList<NewsEvents> newsEvents;

    public ProvincialNewsEventsAdapter(Context context, ArrayList<NewsEvents> newsEvents) {
        this.context = context;
        this.newsEvents = newsEvents;
    }

    //represents layout
    @NonNull
    @Override
    public NewsEventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new NewsEventViewHolder(LayoutInflater.from(context).inflate(R.layout.item_news_events, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull NewsEventViewHolder holder, int position) {
        NewsEvents ne = newsEvents.get(position);
        Glide.with(context)
                .load(ne.getNe_image1())
                .into(holder.binding.image);
        holder.binding.label.setText(ne.getNe_title());
        holder.binding.date.setText(String.valueOf(ne.getNe_created_at()));

        Glide.with(context)
                .load(ne.getMunicipality_image())
                .into(holder.binding.municipalImage);
        holder.binding.municipalityName.setText(String.valueOf(ne.getMunicipality_name()));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, NewsEventsDetailActivity.class);
                intent.putExtra("ne_title", ne.getNe_title());
                intent.putExtra("ne_description", ne.getNe_description());
                intent.putExtra("ne_image", ne.getNe_image1());
                intent.putExtra("ne_created_at", ne.getNe_created_at());
                intent.putExtra("municipality_name", ne.getNe_created_at());
                intent.putExtra("municipality_image", ne.getMunicipality_image());
                intent.putExtra("ne_id", ne.getNe_id());
                intent.putExtra("municipality_id ", ne.getMunicipality_id());
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return newsEvents.size();
    }
    public class NewsEventViewHolder extends RecyclerView.ViewHolder {
        ItemNewsEventsBinding binding;
        public NewsEventViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemNewsEventsBinding.bind(itemView);
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy, hh:mm a", Locale.US);
            dateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Manila"));

        }
    }
}
