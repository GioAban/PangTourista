package pangtourista.project.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieDrawable;
import com.bumptech.glide.Glide;

import java.util.ArrayList;

import pangtourista.project.Models.Municipality;

import pangtourista.project.R;

import pangtourista.project.activities.MunicipalityDetailActivity;
import pangtourista.project.databinding.ItemMunicipalityBinding;


public class MunicipalityAdapter extends  RecyclerView.Adapter<MunicipalityAdapter.MunicipalityViewHolder>{

    Context context;
    ArrayList<Municipality> municipalities;



    public MunicipalityAdapter(Context context, ArrayList<Municipality> municipalities) {
        this.context = context;
        this.municipalities = municipalities;
    }

    //represents layout
    @NonNull
    @Override
    public MunicipalityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MunicipalityViewHolder(LayoutInflater.from(context).inflate(R.layout.item_municipality, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MunicipalityViewHolder holder, int position) {
        Municipality municipality = municipalities.get(position);

        Glide.with(context)
                .load(municipality.getMunicipality_image())
                .into(holder.binding.municipalImage);
        holder.binding.municipalName.setText(municipality.getMunicipality_name());
        holder.binding.municipalAddress.setText(municipality.getMunicipality_address());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, MunicipalityDetailActivity.class);
                intent.putExtra("long", municipality.getMunicipality_lon());
                intent.putExtra("lat", municipality.getMunicipality_lat());
                intent.putExtra("municipality_id", municipality.getMunicipality_id());
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return municipalities.size();
    }
    public class MunicipalityViewHolder extends RecyclerView.ViewHolder {
        ItemMunicipalityBinding binding;
        LottieAnimationView lottie;
        public MunicipalityViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemMunicipalityBinding.bind(itemView);
            lottie = itemView.findViewById(R.id.lottie_location);
            lottie.setRepeatCount(LottieDrawable.INFINITE);
            lottie.playAnimation();
        }
    }
}
