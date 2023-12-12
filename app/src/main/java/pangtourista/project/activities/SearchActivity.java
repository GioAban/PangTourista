package pangtourista.project.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import android.os.Bundle;
import android.view.WindowManager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import pangtourista.project.Adapters.LandmarkAdapter;
import pangtourista.project.Models.Landmark;
import pangtourista.project.databinding.ActivitySearchBinding;
import pangtourista.project.utils.Constants;

public class SearchActivity extends AppCompatActivity {

    ActivitySearchBinding binding;
    LandmarkAdapter landmarkAdapter;
    ArrayList<Landmark> landmarks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        binding = ActivitySearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        landmarks = new ArrayList<>();
        landmarkAdapter = new LandmarkAdapter(getApplicationContext(), landmarks);


        String query = getIntent().getStringExtra("query");

        getLandmarks(query);

        GridLayoutManager layoutManager = new GridLayoutManager(getApplicationContext(), 2);
        binding.landmarkList.setLayoutManager(layoutManager);
        binding.landmarkList.setAdapter(landmarkAdapter);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

    void getLandmarks(String query){
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = Constants.GET_PRODUCTS_URL + "?q=" + query;
        StringRequest request = new StringRequest(Request.Method.GET, url, response -> {
            try {
                JSONObject object = new JSONObject(response);
                if (object.getString("status").equals("success")){
                    JSONArray productsArray = object.getJSONArray("products");
                    for(int i=0; i < productsArray.length(); i++){
                        JSONObject childObj = productsArray.getJSONObject(i);
                        Landmark landmark = new Landmark(
                                childObj.getString("landmark_name"),
                                childObj.getString("address"),
                                Constants.LANDMARK_IMAGE_URL + childObj.getString("landmark_img_1"),
                                childObj.getString("description_1"),
                                childObj.getString("longitude"),
                                childObj.getString("latitude"),
                                childObj.getInt("landmark_id")

                        );
                        landmarks.add(landmark);
                    }
                    landmarkAdapter.notifyDataSetChanged();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> { });
        queue.add(request);
    }
}