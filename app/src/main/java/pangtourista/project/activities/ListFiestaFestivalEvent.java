package pangtourista.project.activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.chip.Chip;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


import pangtourista.project.Adapters.FiestaFestivalEventAdapter;
import pangtourista.project.Models.FiestaFestivalEvent;

import pangtourista.project.R;
import pangtourista.project.databinding.ActivityListFiestaFestivalEventBinding;
import pangtourista.project.utils.Constants;

public class ListFiestaFestivalEvent extends AppCompatActivity {

    ActivityListFiestaFestivalEventBinding binding;
    FiestaFestivalEventAdapter fiestaFestivalEventAdapter;
    ArrayList<FiestaFestivalEvent> fiestaFestivalEvents;
    Button btnAll, close_va;
    AlertDialog dialog;
    TextView description;

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        binding = ActivityListFiestaFestivalEventBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        toolbar = findViewById(R.id.fiesta);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("List of Fiestas & Festivals");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        int whiteColor = ContextCompat.getColor(this, android.R.color.white);
        toolbar.setTitleTextColor(whiteColor);
        setNavigationIconColor(toolbar, whiteColor);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Fiesta/Festival Description");
        View view = getLayoutInflater().inflate(R.layout.modal_fiesta_festival, null);
        //View dialog
        builder.setView(view);
        //Create dialog
        dialog = builder.create();
        description = view.findViewById(R.id.description);
        close_va = view.findViewById(R.id.close_festival);
        close_va.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        fiestaFestivalEvents = new ArrayList<>();
        fiestaFestivalEventAdapter = new FiestaFestivalEventAdapter(getApplicationContext(), fiestaFestivalEvents);
        GridLayoutManager layoutManager = new GridLayoutManager(getApplicationContext(), 1);
        binding.fiestaFestivalEventList.setLayoutManager(layoutManager);
        binding.fiestaFestivalEventList.setAdapter(fiestaFestivalEventAdapter);

        getListOfFiestaFestivalEvent();


    }

    void getListOfFiestaFestivalEvent() {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = Constants.API_BASE_URL+"/users/get-fiesta-festival-event.php";
        StringRequest request = new StringRequest(Request.Method.GET, url, response -> {
            try {
                JSONObject object = new JSONObject(response);
                if(object.getString("status").equals("success")){
                    JSONArray neArray = object.getJSONArray("fiesta_festival_events");
                    for(int i =0; i< neArray.length(); i++) {
                        JSONObject childObj = neArray.getJSONObject(i);
                        FiestaFestivalEvent fiestaFestivalEvent = new FiestaFestivalEvent(
                                childObj.getInt("event_id"),
                                childObj.getInt("municipality_id"),
                                childObj.getString("event_title"),
                                childObj.getString("event_date_start"),
                                childObj.getString("event_date_end"),
                                childObj.getString("municipality_name"),
                                childObj.getString("description")
                        );
                        fiestaFestivalEvents.add(fiestaFestivalEvent);
                    }
                    fiestaFestivalEventAdapter.notifyDataSetChanged();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> { });

        queue.add(request);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle the back button click
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setNavigationIconColor(androidx.appcompat.widget.Toolbar toolbar, int color) {
        if (toolbar.getNavigationIcon() != null) {
            toolbar.getNavigationIcon().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        }
    }

}