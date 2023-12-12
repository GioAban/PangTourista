package pangtourista.project.activities;

import static pangtourista.project.utils.Constants.MUNICIPALITY_IMAGE_URL;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

import ai.nextbillion.kits.geojson.Point;
import pangtourista.project.R;
import pangtourista.project.utils.Constants;

public class SearchMunicipality extends AppCompatActivity {


    TextView displayData;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_municipality);


        String municipality_name = getIntent().getStringExtra("municipality_name");

        RequestQueue queue = Volley.newRequestQueue(this);
        String url = Constants.API_BASE_URL + "/users/search-municipality.php?municipality_name=" + municipality_name;
        StringRequest request = new StringRequest(Request.Method.GET, url, response -> {
            try {
                JSONObject object = new JSONObject(response);
                String status = object.getString("status");
                if ("success".equals(status)) {
                    JSONArray municipalitiesArray = object.getJSONArray("municipalities"); // Use getJSONArray to get the array
                    if (municipalitiesArray.length() > 0) {
                        // Assuming you want the first item in the array
                        JSONObject municipality = municipalitiesArray.getJSONObject(0);
                        String municipality_lon = municipality.getString("municipality_lon");
                        String municipality_lat = municipality.getString("municipality_lat");
                        Toast.makeText(this, municipality_lon + " " + municipality_lat, Toast.LENGTH_SHORT).show();
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
}