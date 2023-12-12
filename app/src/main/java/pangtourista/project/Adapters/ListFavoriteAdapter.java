package pangtourista.project.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import pangtourista.project.Models.Favorite;
import pangtourista.project.Models.Landmark;
import pangtourista.project.R;
import pangtourista.project.activities.LandmarkDetailActivity;
import pangtourista.project.activities.ListFavorite;
import pangtourista.project.activities.MunicipalityDetailActivity;
import pangtourista.project.databinding.ItemFavoriteBinding;

public class ListFavoriteAdapter extends RecyclerView.Adapter<ListFavoriteAdapter.ListFavoriteViewHolder> {

    Context context;
    ArrayList<Favorite> favorites;
    public ListFavoriteAdapter(Context context, ArrayList<Favorite> favorites){
        this.context = context;
        this.favorites = favorites;
    }

    @NonNull
    @Override
    public ListFavoriteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ListFavoriteViewHolder(LayoutInflater.from(context).inflate(R.layout.item_favorite, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ListFavoriteViewHolder holder, int position) {
        Favorite favorite = favorites.get(position);
        holder.binding.landmarkName.setText(favorite.getLandmarkName());
        holder.binding.category.setText(favorite.getCategory());
        Glide.with(context).load(favorite.getLandmarkImage()).into(holder.binding.landmarkImage);
        holder.binding.municipalName.setText(favorite.getMunicipalityName());

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(context, "delete the saved landmark" + favorite.getFavorite_id(), Toast.LENGTH_SHORT).show();
                return false;
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, LandmarkDetailActivity.class);
                intent.putExtra("lat", favorite.getLatitude());
                intent.putExtra("long", favorite.getLongitude());
                intent.putExtra("id", favorite.getLandmarkId());
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return favorites.size();
    }

    public class ListFavoriteViewHolder extends RecyclerView.ViewHolder {
        ItemFavoriteBinding binding;
        public ListFavoriteViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemFavoriteBinding.bind(itemView);
        }
    }
}
