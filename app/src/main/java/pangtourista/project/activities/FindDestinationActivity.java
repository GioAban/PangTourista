package pangtourista.project.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;


import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.mancj.materialsearchbar.MaterialSearchBar;

import org.imaginativeworld.whynotimagecarousel.model.CarouselItem;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import pangtourista.project.Models.NewsEvents;
import pangtourista.project.R;
import pangtourista.project.databinding.ActivityFindDestinationBinding;
import pangtourista.project.utils.Constants;


public class FindDestinationActivity extends AppCompatActivity {

    ActivityFindDestinationBinding binding;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_destination);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        binding = ActivityFindDestinationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        toolbar = findViewById(R.id.findDestination);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Find destination");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        int whiteColor = ContextCompat.getColor(this, android.R.color.white);
        toolbar.setTitleTextColor(whiteColor);
        setNavigationIconColor(toolbar, whiteColor);

        initSlider();
        featuring_initSlider();

        //Search Municipality Name
        binding.searchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {

            }

            @Override
            public void onSearchConfirmed(CharSequence landmark_name) {
                RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
                String url = Constants.API_BASE_URL + "/users/search-destination.php?landmark_name=" + landmark_name;
                StringRequest request = new StringRequest(Request.Method.GET, url, response -> {
                    try {
                        JSONObject object = new JSONObject(response);
                        String status = object.getString("status");
                        if ("success".equals(status)) {
                            JSONArray landmarkArray = object.getJSONArray("landmarks"); // Use getJSONArray to get the array
                            if (landmarkArray.length() > 0) {
                                // Assuming you want the first item in the array
                                JSONObject landmark = landmarkArray.getJSONObject(0);
                                int landmark_id = landmark.getInt("landmark_id");
                                String longitude = landmark.getString("longitude");
                                String latitude = landmark.getString("latitude");
                                Intent intent = new Intent(FindDestinationActivity.this, LandmarkDetailActivity.class);
                                intent.putExtra("id", landmark_id);
                                intent.putExtra("long", longitude);
                                intent.putExtra("lat", latitude);
                                startActivity(intent);
                            } else {
                                // Handle the case when no municipalities were found in the array
                            }
                        } else if ("empty".equals(status)) {
                            // Handle the case when the "status" is "empty"
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
            public void onButtonClicked(int buttonCode) {

            }
        });

    }

    private void initSlider() {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = Constants.API_BASE_URL + "/users/get-most-viewed-landmark.php";
        StringRequest request = new StringRequest(Request.Method.GET, url, response -> {
            try {
                JSONObject object = new JSONObject(response);
                if (object.getString("status").equals("success")) {
                    JSONArray neArray = object.getJSONArray("landmarks");
                    for (int i = 0; i < neArray.length(); i++) {
                        JSONObject childObj = neArray.getJSONObject(i);
                        binding.carousel.addData(
                                new CarouselItem(
                                        Constants.LANDMARK_IMAGE_URL + childObj.getString("landmark_img_1"),
                                        childObj.getString("landmark_name")  // Use landmark name as the caption
                                )
                        );
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> {
            // Handle error here
        });
        queue.add(request);
    }

    private void featuring_initSlider() {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = Constants.API_BASE_URL + "/users/get-featuring-landmark.php";
        StringRequest request = new StringRequest(Request.Method.GET, url, response -> {
            try {
                JSONObject object = new JSONObject(response);
                if (object.getString("status").equals("success")) {
                    JSONArray neArray = object.getJSONArray("landmarks");
                    for (int i = 0; i < neArray.length(); i++) {
                        JSONObject childObj = neArray.getJSONObject(i);
                        binding.featuringCarousel.addData(
                                new CarouselItem(
                                        Constants.LANDMARK_IMAGE_URL + childObj.getString("landmark_img_1"),
                                        childObj.getString("landmark_name")  // Use landmark name as the caption
                                )
                        );
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> {
            // Handle error here
        });
        queue.add(request);
    }

    public void goToVirtualAssistantActivity(View view) {
        Intent intent = new Intent(getApplicationContext(), VistualAssistantActivity.class);
        Animatoo.animateFade(FindDestinationActivity.this);
        startActivity(intent);
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