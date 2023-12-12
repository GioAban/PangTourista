package pangtourista.project.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import pangtourista.project.Models.Landmark;
import pangtourista.project.R;
import pangtourista.project.activities.LandmarkDetailActivity;
import pangtourista.project.databinding.ItemMunicipalityLandmarkListBinding;

public class MunicipalityLandmarkAdapter extends  RecyclerView.Adapter<MunicipalityLandmarkAdapter.landmarkViewHolder>{
    Context context;
    ArrayList<Landmark> landmarks;
    public MunicipalityLandmarkAdapter(Context context, ArrayList<Landmark> products) {
        this.context = context;
        this.landmarks = products;
    }

    //represents layout
    @NonNull
    @Override
    public landmarkViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new landmarkViewHolder(LayoutInflater.from(context).inflate(R.layout.item_municipality_landmark_list, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull landmarkViewHolder holder, int position) {
        Landmark landmark = landmarks.get(position);
        Glide.with(context)
                .load(landmark.getLandmark_img_1())
                .into(holder.binding.municipalLandmarkImage);
        holder.binding.municipalLandmarkName.setText(landmark.getLandmark_name());
        holder.binding.landmarkAddress.setText(landmark.getAddress());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, LandmarkDetailActivity.class);
                intent.putExtra("name", landmark.getLandmark_name());
                intent.putExtra("image", landmark.getLandmark_img_1());
                intent.putExtra("id", landmark.getLandmark_id());
                intent.putExtra("description", landmark.getDescription_1());
                intent.putExtra("lat", landmark.getLatitude());
                intent.putExtra("long", landmark.getLongitude());
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);

            }
        });
    }

    @Override
    public int getItemCount() {
        return landmarks.size();
    }

    public class landmarkViewHolder extends RecyclerView.ViewHolder {
        ItemMunicipalityLandmarkListBinding binding;
        public landmarkViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemMunicipalityLandmarkListBinding.bind(itemView);
        }
    }
}
