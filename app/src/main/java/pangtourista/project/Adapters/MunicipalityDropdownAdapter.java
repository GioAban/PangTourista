package pangtourista.project.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import pangtourista.project.Models.Municipality;
import pangtourista.project.R;
import pangtourista.project.activities.MunicipalityDetailActivity;
import pangtourista.project.databinding.ItemMunicipalityDropdownBinding;


public class MunicipalityDropdownAdapter extends RecyclerView.Adapter<MunicipalityDropdownAdapter.MunicipalityDropdownViewHolder> {

    Context context;
    ArrayList<Municipality> municipalities;

    public MunicipalityDropdownAdapter(Context context, ArrayList<Municipality> municipalities) {
        this.context = context;
        this.municipalities = municipalities;
    }

    @NonNull
    @Override
    public MunicipalityDropdownViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MunicipalityDropdownViewHolder(LayoutInflater.from(context).inflate(R.layout.item_municipality_dropdown, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MunicipalityDropdownViewHolder holder, int position) {
        Municipality municipality = municipalities.get(position);
        holder.binding.municipalityName.setText(municipality.getMunicipality_name());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, MunicipalityDetailActivity.class);
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

    public class MunicipalityDropdownViewHolder extends RecyclerView.ViewHolder {
        ItemMunicipalityDropdownBinding binding;
        public MunicipalityDropdownViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemMunicipalityDropdownBinding.bind(itemView);
        }
    }
}
