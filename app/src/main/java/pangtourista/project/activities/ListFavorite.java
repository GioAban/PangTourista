package pangtourista.project.activities;

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;


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


import pangtourista.project.Adapters.ListFavoriteAdapter;
import pangtourista.project.Models.Favorite;
import pangtourista.project.Models.Landmark;
import pangtourista.project.R;
import pangtourista.project.Sessions.SessionManager;
import pangtourista.project.databinding.ActivityListFavoriteBinding;
import pangtourista.project.utils.Constants;

public class ListFavorite extends AppCompatActivity {

    ActivityListFavoriteBinding binding;
    ListFavoriteAdapter favoriteAdapter;
    ArrayList<Favorite> favoritesArr;
    SessionManager sessionManager;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        binding = ActivityListFavoriteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        toolbar = findViewById(R.id.favorite);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Saved");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        int whiteColor = ContextCompat.getColor(this, android.R.color.white);
        toolbar.setTitleTextColor(whiteColor);
        setNavigationIconColor(toolbar, whiteColor);


        favoritesArr = new ArrayList<>();
        favoriteAdapter = new ListFavoriteAdapter(getApplicationContext(), favoritesArr);
        GridLayoutManager layoutManager = new GridLayoutManager(getApplicationContext(), 1);
        binding.favoriteList.setLayoutManager(layoutManager);
        binding.favoriteList.setAdapter(favoriteAdapter);


        sessionManager = new SessionManager(this);
        String user_id = sessionManager.getUserDetail().get("USER_ID");
        getFavorite(user_id);

    }
    void getFavorite(String user_id) {
        String url = Constants.API_BASE_URL+"/users/get-favorite-landmark.php?user_id="+user_id;
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.getString("status").equals("success")) {
                                JSONArray productsArray = response.getJSONArray("favorites");
                                for (int i = 0; i < productsArray.length(); i++) {
                                    JSONObject childObj = productsArray.getJSONObject(i);
                                    Favorite favorite = new Favorite(
                                            childObj.getInt("favorite_id"),
                                            childObj.getString("landmark_name"),
                                            childObj.getString("category"),
                                            childObj.getString("longitude"),
                                            childObj.getString("latitude"),
                                            childObj.getInt("landmark_id"),
                                            Constants.LANDMARK_IMAGE_URL + childObj.getString("landmark_img_1"),
                                            childObj.getString("municipality_name")
                                    );
                                    favoritesArr.add(favorite);
                                }

                                // Update the landmarks ArrayList with the fetched data
//                                ListFavorite.this.favorites.clear();
//                                ListFavorite.this.favorites.addAll(favorites);

                                // Notify the adapter about the data change
                                favoriteAdapter.notifyDataSetChanged();
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
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
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