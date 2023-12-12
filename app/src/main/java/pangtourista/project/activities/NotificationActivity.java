package pangtourista.project.activities;

import static pangtourista.project.utils.Constants.USER_IMAGE_URL;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import pangtourista.project.Adapters.NotificationAdapter;
import pangtourista.project.Models.Landmark;
import pangtourista.project.Models.Municipality;
import pangtourista.project.Models.MunicipalityDropdown;
import pangtourista.project.Models.Notification;
import pangtourista.project.R;
import pangtourista.project.databinding.ActivityNotificationBinding;
import pangtourista.project.utils.Constants;

public class NotificationActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    NotificationAdapter notificationAdapter;
    ArrayList<Notification> notifications;
    ActivityNotificationBinding binding;
    private Button btnAll, btnProvincial, btnMunicipality;
    private Toolbar toolbar;

    Spinner spinnerMunicipality;
    ArrayList<String> municipalityList = new ArrayList<>();
    ArrayAdapter<String> municipalityAdapter;
    RequestQueue requestQueue;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        binding = ActivityNotificationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        toolbar = findViewById(R.id.notification_bar_layout);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Notifications");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        int whiteColor = ContextCompat.getColor(this, android.R.color.white);
        toolbar.setTitleTextColor(whiteColor);
        setNavigationIconColor(toolbar, whiteColor);

        notifications = new ArrayList<>();
        notificationAdapter = new NotificationAdapter(NotificationActivity.this, notifications);

        String query = "get-all-notification-list.php";
        getNotification(query);

        btnAll = findViewById(R.id.btnAll);
        btnProvincial = findViewById(R.id.btnProvincial);
        btnMunicipality = findViewById(R.id.btnMunicipality);
        spinnerMunicipality = findViewById(R.id.spinnerMunicipality);
        requestQueue = Volley.newRequestQueue(this);

        // Set click listeners for buttons
        btnAll.setOnClickListener(v -> {
            spinnerMunicipality.setVisibility(View.VISIBLE);
            String get_all_query = "get-all-notification-list.php";
            getNotification(get_all_query);
            notifications.clear();
            notificationAdapter.notifyDataSetChanged();

        });

        btnProvincial.setOnClickListener(v -> {
            spinnerMunicipality.setVisibility(View.GONE);
            String get_provincial_query = "get-provincial-notification-list.php";
            getNotification(get_provincial_query);
            notifications.clear();
            notificationAdapter.notifyDataSetChanged();


        });

        btnMunicipality.setOnClickListener(v -> {
            spinnerMunicipality.setVisibility(View.VISIBLE);
            String get_municipality_query = "get-municipality-notification-list.php";
            getNotification(get_municipality_query);
            notifications.clear();
            notificationAdapter.notifyDataSetChanged();
        });

        GridLayoutManager layoutManager = new GridLayoutManager(getApplicationContext(), 1);
        binding.notificationList.setLayoutManager(layoutManager);
        binding.notificationList.setAdapter(notificationAdapter);

        String url = Constants.API_BASE_URL + "/users/get-municipality-list.php";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    municipalityList.clear();
                    municipalityList.add("Select Municipality/City");
                    JSONArray jsonArray = response.getJSONArray("municipalities");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        String municipality_name = jsonObject.optString("municipality_name");
                        String municipality_id = jsonObject.optString("municipality_id");
                        municipalityList.add(municipality_name);
                        municipalityAdapter = new ArrayAdapter<>(NotificationActivity.this, android.R.layout.simple_spinner_item, municipalityList);
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

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

    void getNotification(String query) {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = Constants.API_BASE_URL + "/users/" + query;
        StringRequest request = new StringRequest(Request.Method.GET, url, response -> {
            try {
                JSONObject object = new JSONObject(response);
                if (object.getString("status").equals("success")) {
                    JSONArray neArray = object.getJSONArray("notifications");
                    for (int i = 0; i < neArray.length(); i++) {
                        JSONObject childObj = neArray.getJSONObject(i);
                        Notification ne = new Notification(
                                childObj.getString("ne_title"),
                                Constants.MUNICIPALITY_IMAGE_URL + childObj.getString("municipality_image"),
                                childObj.getString("municipality_name"),
                                childObj.getString("ne_created_at"),
                                childObj.getInt("ne_id"),
                                childObj.getInt("municipality_id")
                        );
                        notifications.add(ne);
                    }
                    notificationAdapter.notifyDataSetChanged();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> {
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

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        // Handle the case where nothing is selected if needed
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
                        String get_municipality_query = "get-municipality-notification-list-filtered.php?municipality_id=" + municipality_id;
                        getNotification(get_municipality_query);
                        notifications.clear();
                        notificationAdapter.notifyDataSetChanged();
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
}
