package pangtourista.project.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;

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

import pangtourista.project.Adapters.LandmarkAdapter;
import pangtourista.project.Models.Landmark;
import pangtourista.project.R;
import pangtourista.project.databinding.ActivityCategoryBinding;
import pangtourista.project.utils.Constants;

public class CategoryActivity extends AppCompatActivity {

    ActivityCategoryBinding binding;
    LandmarkAdapter landmarkAdapter;
    ArrayList<Landmark> landmarks;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        binding = ActivityCategoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        landmarks = new ArrayList<>();
        landmarkAdapter = new LandmarkAdapter(getApplicationContext(), landmarks);

        int catId = getIntent().getIntExtra("catId", 0);
        String category = getIntent().getStringExtra("categoryName");

        toolbar = findViewById(R.id.category_of_destination);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(category);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        int whiteColor = ContextCompat.getColor(this, android.R.color.white);
        toolbar.setTitleTextColor(whiteColor);
        setNavigationIconColor(toolbar, whiteColor);

        getLandmark(category);

        GridLayoutManager layoutManager = new GridLayoutManager(getApplicationContext(), 1);
        binding.landmarkList.setLayoutManager(layoutManager);
        binding.landmarkList.setAdapter(landmarkAdapter);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

    void getLandmark(String category) {
        String url = Constants.API_BASE_URL+"/users/click-category.php?category=" + category;
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.getString("status").equals("success")) {
                                JSONArray productsArray = response.getJSONArray("landmarks");
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
                                CategoryActivity.this.landmarks.clear();
                                CategoryActivity.this.landmarks.addAll(landmarks);

                                // Notify the adapter about the data change
                                landmarkAdapter.notifyDataSetChanged();
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

    private void setNavigationIconColor(androidx.appcompat.widget.Toolbar toolbar, int color) {
        if (toolbar.getNavigationIcon() != null) {
            toolbar.getNavigationIcon().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        }
    }
}