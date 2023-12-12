package pangtourista.project.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.WindowManager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import pangtourista.project.Adapters.MunicipalityLandmarkAdapter;
import pangtourista.project.Models.Landmark;
import pangtourista.project.R;
import pangtourista.project.databinding.ActivityMunicipalityDetailBinding;
import pangtourista.project.databinding.ActivitySeeMunicipalityBinding;
import pangtourista.project.utils.Constants;


public class SeeMunicipalityActivity extends AppCompatActivity {

    ActivitySeeMunicipalityBinding binding;
    ArrayList<Landmark> landmarks;
    private Toolbar toolbar;
    MunicipalityLandmarkAdapter municipalityLandmarkAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        binding = ActivitySeeMunicipalityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        toolbar = findViewById(R.id.seeMunicipality);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Attractions");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        int whiteColor = ContextCompat.getColor(this, android.R.color.white);
        toolbar.setTitleTextColor(whiteColor);
        setNavigationIconColor(toolbar, whiteColor);


        int municipality_id = getIntent().getIntExtra("municipality_id",0);
        getMunicipalityLandmark(municipality_id);

        landmarks = new ArrayList<>();
        municipalityLandmarkAdapter = new MunicipalityLandmarkAdapter(getApplicationContext(), landmarks);


        RecyclerView recyclerView = findViewById(R.id.municipal_landmark_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);

        binding.municipalLandmarkList.setLayoutManager(layoutManager);
        binding.municipalLandmarkList.setAdapter(municipalityLandmarkAdapter);

    }

    private void getMunicipalityLandmark(int municipality_id) {
        String url = Constants.API_BASE_URL+"/users/get-municipality-landmark.php?municipality_id=" + municipality_id;
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.getString("status").equals("success")) {
                                JSONArray productsArray = response.getJSONArray("municipality_landmark");
                                ArrayList<Landmark> landmarks = new ArrayList<>();
                                for (int i = 0; i < productsArray.length(); i++) {
                                    JSONObject childObj = productsArray.getJSONObject(i);
                                    Landmark product = new Landmark(
                                            childObj.getString("landmark_name"),
                                            childObj.getString("address"),
                                            Constants.LANDMARK_IMAGE_URL + childObj.getString("landmark_img_1"),
                                            childObj.getString("description_1"),
                                            childObj.getString("longitude"),
                                            childObj.getString("latitude"),
                                            childObj.getInt("landmark_id")
                                    );
                                    landmarks.add(product);
                                }

                                // Update the landmarks ArrayList with the fetched data
                                SeeMunicipalityActivity.this.landmarks.clear();
                                SeeMunicipalityActivity.this.landmarks.addAll(landmarks);

                                // Notify the adapter about the data change
                                municipalityLandmarkAdapter.notifyDataSetChanged();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle Volley error here
                    }
                });

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

    private void setNavigationIconColor(Toolbar toolbar, int color) {
        if (toolbar.getNavigationIcon() != null) {
            toolbar.getNavigationIcon().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        }
    }

}