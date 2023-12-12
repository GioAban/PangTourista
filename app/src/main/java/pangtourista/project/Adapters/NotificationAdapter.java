package pangtourista.project.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import pangtourista.project.Models.Notification;
import pangtourista.project.R;
import pangtourista.project.activities.NewsEventsDetailActivity;
import pangtourista.project.databinding.ItemNotificationBinding;


public class NotificationAdapter extends  RecyclerView.Adapter<NotificationAdapter.notificationViewHolder>{
    Context context;
    ArrayList<Notification> notifications;
    public NotificationAdapter(Context context, ArrayList<Notification> notifications) {
        this.context = context;
        this.notifications = notifications;
    }

    //represents layout
    @NonNull
    @Override
    public notificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new notificationViewHolder(LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull notificationViewHolder holder, int position) {
        Notification notification = notifications.get(position);
        Glide.with(context)
        .load(notification.getMunicipality_image())
        .into(holder.binding.municipalImage);
        holder.binding.municipalName.setText(notification.getMunicipality_name());
        holder.binding.notificationTitle.setText(notification.getNe_title());
        holder.binding.notificationDate.setText(notification.getNe_created_at());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, NewsEventsDetailActivity.class);
                intent.putExtra("ne_title", notification.getNe_title());
                intent.putExtra("ne_created_at", notification.getNe_created_at());
                intent.putExtra("municipality_name", notification.getNe_created_at());
                intent.putExtra("municipality_image", notification.getMunicipality_image());
                intent.putExtra("ne_id", notification.getNe_id());
                intent.putExtra("municipality_id ", notification.getMunicipality_id());
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    public class notificationViewHolder extends RecyclerView.ViewHolder {
        ItemNotificationBinding binding;
        public notificationViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemNotificationBinding.bind(itemView);
        }
    }
}
