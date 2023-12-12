package pangtourista.project.activities;

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieDrawable;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import pangtourista.project.Adapters.NewsEventsAdapter;
import pangtourista.project.Models.NewsEvents;
import pangtourista.project.R;
import pangtourista.project.databinding.ActivityNewsEventBinding;
import pangtourista.project.utils.Constants;

public class NewsEventActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private NewsEventsAdapter newsEventsAdapter;
    private ArrayList<NewsEvents> newsEvents;
    private ActivityNewsEventBinding binding;

    private Button btnAll, btnProvincial, btnMunicipality;

    private Toolbar toolbar;

    Spinner spinnerMunicipality;

    RequestQueue requestQueue;
    ArrayList<String> municipalityList = new ArrayList<>();
    ArrayAdapter<String> municipalityAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        binding = ActivityNewsEventBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        toolbar = findViewById(R.id.news);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("News & Events");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        int whiteColor = ContextCompat.getColor(this, android.R.color.white);
        toolbar.setTitleTextColor(whiteColor);
        setNavigationIconColor(toolbar, whiteColor);
        spinnerMunicipality = findViewById(R.id.spinnerMunicipality);
        requestQueue = Volley.newRequestQueue(this);



        // Initialize views and load the initial data
        initAllNewsEvents();

        btnAll = findViewById(R.id.btnAll);
        btnProvincial = findViewById(R.id.btnProvincial);
        btnMunicipality = findViewById(R.id.btnMunicipality);

        // Set click listeners for buttons
        btnAll.setOnClickListener(v -> {
            String query = "get-all-news-events-list.php";
            getAllNewsEvents(query);
            newsEvents.clear();
            newsEventsAdapter.notifyDataSetChanged();
            spinnerMunicipality.setVisibility(View.VISIBLE);


        });

        btnProvincial.setOnClickListener(v -> {
            String query = "get-provincial-news-events-list.php";
            getAllNewsEvents(query);
            newsEvents.clear();
            newsEventsAdapter.notifyDataSetChanged();
            spinnerMunicipality.setVisibility(View.GONE);


        });

        btnMunicipality.setOnClickListener(v -> {
            String query = "get-municipality-news-events-list.php";
            getAllNewsEvents(query);
            newsEvents.clear();
            newsEventsAdapter.notifyDataSetChanged();
            spinnerMunicipality.setVisibility(View.VISIBLE);
        });

        String url = Constants.API_BASE_URL + "/users/get-municipality-list.php";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    municipalityList.clear();
                    municipalityList.add("Select Municipality/city");
                    JSONArray jsonArray = response.getJSONArray("municipalities");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        String municipality_name = jsonObject.optString("municipality_name");
                        municipalityList.add(municipality_name);
                        municipalityAdapter = new ArrayAdapter<>(NewsEventActivity.this, android.R.layout.simple_spinner_item, municipalityList);
                        municipalityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerMunicipality.setAdapter(municipalityAdapter);
                    }
                } catch (JSONException e) {

                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        requestQueue.add(jsonObjectRequest);
        spinnerMunicipality.setOnItemSelectedListener(this);
    }

    private void initAllNewsEvents() {
        newsEvents = new ArrayList<>();
        newsEventsAdapter = new NewsEventsAdapter(this, newsEvents);
        String query = "get-all-news-events-list.php";

        // Don't clear the list and notify adapter here since you will do it in getAllNewsEvents
        GridLayoutManager layoutManagerAll = new GridLayoutManager(this, 1);
        binding.newsEventsList.setLayoutManager(layoutManagerAll);
        binding.newsEventsList.setAdapter(newsEventsAdapter);
        // Load the initial data
        getAllNewsEvents(query);
    }

    private void getAllNewsEvents(String query) {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = Constants.API_BASE_URL + "/users/" + query;
        StringRequest request = new StringRequest(Request.Method.GET, url, response -> {
            try {
                JSONObject object = new JSONObject(response);
                if (object.getString("status").equals("success")) {
                    JSONArray neArray = object.getJSONArray("news_events");
                    newsEvents.clear(); // Clear existing data before adding new data
                    for (int i = 0; i < neArray.length(); i++) {
                        JSONObject childObj = neArray.getJSONObject(i);
                        NewsEvents ne = new NewsEvents(
                                childObj.getString("ne_title"),
                                childObj.getString("ne_description"),
                                Constants.NEWS_EVENTS_IMAGE_URL + childObj.getString("ne_image"),
                                childObj.getString("ne_created_at"),
                                childObj.getString("municipality_name"),
                                Constants.MUNICIPALITY_IMAGE_URL + childObj.getString("municipality_image"),
                                childObj.getInt("ne_id"),
                                childObj.getInt("municipality_id")
                        );
                        newsEvents.add(ne);
                    }
                    newsEventsAdapter.notifyDataSetChanged(); // Notify adapter after adding new data
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> {
            // Handle error here
        });
        queue.add(request);
    }
    //Spinner property
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

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
        if (adapterView.getId() == R.id.spinnerMunicipality) {
            String selectedMunicipality = adapterView.getSelectedItem().toString();
            if (selectedMunicipality.equals("Select Municipality/City")){


            } else {
                getMunicipalityNews(selectedMunicipality);
            }
        }
    }

    void getMunicipalityNews(String municipality_name) {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = Constants.API_BASE_URL + "/users/select-municipality-name.php?municipality_name=" + municipality_name;
        StringRequest request = new StringRequest(Request.Method.GET, url, response -> {
            try {
                JSONObject object = new JSONObject(response);
                if (object.getString("status").equals("success")) {
                    JSONArray landmarksArray = object.getJSONArray("landmarks");
                    if (landmarksArray.length() > 0) {
                        JSONObject landmark = landmarksArray.getJSONObject(0);
                        String municipality_id = landmark.getString("municipality_id");
                        String query = "get-municipality-news-events-list-filtered.php?municipality_id=" + municipality_id;
                        getAllNewsEvents(query);
                        newsEvents.clear();
                        newsEventsAdapter.notifyDataSetChanged();
                    } else {
                        // Handle the case when no landmarks were found
                    }
                } else if (object.getString("status").equals("empty")) {

                } else {
                    // Handle the error case
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Handle error response
            }
        });
        queue.add(request);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
