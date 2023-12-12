package pangtourista.project.activities;

import static pangtourista.project.utils.Constants.MUNICIPALITY_IMAGE_URL;
import static pangtourista.project.utils.Constants.USER_IMAGE_URL;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;


import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieDrawable;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.bumptech.glide.Glide;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import com.mancj.materialsearchbar.MaterialSearchBar;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;

import ai.nextbillion.kits.geojson.Point;
import de.hdodenhof.circleimageview.CircleImageView;
import pangtourista.project.Adapters.CategoryAdapter;
import pangtourista.project.Adapters.LandmarkAdapter;
import pangtourista.project.Adapters.MunicipalityAdapter;
import pangtourista.project.Adapters.NewsEventsAdapter;
import pangtourista.project.EmailLoginRegister.EmailLoginActivity;
import pangtourista.project.Models.Category;
import pangtourista.project.Models.Landmark;
import pangtourista.project.Models.Municipality;
import pangtourista.project.Models.NewsEvents;
import pangtourista.project.R;
import pangtourista.project.Sessions.SessionManager;
import pangtourista.project.databinding.ActivityMainBinding;
import pangtourista.project.utils.Constants;


public class MainActivity extends AppCompatActivity {
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    CircleImageView profile_image;
    TextView header_nav_username, header_nav_email;
    CircleImageView logged_user_photo;
    ActionBarDrawerToggle drawerToggle;
    ActivityMainBinding binding;
    CategoryAdapter categoryAdapter;
    ArrayList<Category> categories;
    LandmarkAdapter landmarkAdapter;
    ArrayList<Landmark> landmarks;
    MunicipalityAdapter municipalityAdapter;
    ArrayList<Municipality> municipalities;
    SessionManager sessionManager;
    CardView btnAdventure, btnCulturalHeritage, btnChurch, btnFamily, btnProduct, btnFarmAndNature, btnIslandAndBeaches, btnRidgeAndReef, btnOther;

    //Location related entities
    private LocationRequest locationRequest;
    private final static int REQUEST_CODE = 100;
    private double setLatitude;
    private double setLongitude;

    FloatingActionButton addAlarmFab, addPersonFab;
    ExtendedFloatingActionButton addActionsFab;
    TextView addAlarmText, addPersonText;
    Boolean isAllFABVisible;

    AlertDialog dialog;
    Button close_va;
    TextView virtualGuide;

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(this);
        String user_id = sessionManager.getUserDetail().get("USER_ID");
        btnAdventure = findViewById(R.id.btnAdventure);
        btnCulturalHeritage = findViewById(R.id.btnCulturalHeritage);
        btnChurch = findViewById(R.id.btnChurch);
        btnFamily = findViewById(R.id.btnFamily);
        btnProduct = findViewById(R.id.btnProduct);
        btnFarmAndNature = findViewById(R.id.btnFarmAndNature);
        btnIslandAndBeaches = findViewById(R.id.btnIslandAndBeaches);
        btnRidgeAndReef = findViewById(R.id.btnRidgeAndReef);
        btnOther = findViewById(R.id.btnOther);
        logged_user_photo = findViewById(R.id.logged_user_photo);
        Toolbar toolbar = findViewById(R.id.toolbar);

        addAlarmFab = findViewById(R.id.add_alarm);
        addPersonFab = findViewById(R.id.add_person);
        addActionsFab = findViewById(R.id.add_fab);

        addAlarmText = findViewById(R.id.add_alarm_text);
        addPersonText = findViewById(R.id.add_person_text);

        addAlarmFab.setVisibility(View.GONE);
        addPersonFab.setVisibility(View.GONE);
        addAlarmText.setVisibility(View.GONE);
        addPersonText.setVisibility(View.GONE);

        isAllFABVisible = false;
        addActionsFab.shrink();
        addActionsFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if (!isAllFABVisible) {
//                    addAlarmFab.show();
//                    addPersonFab.show();
//                    addAlarmText.setVisibility(View.VISIBLE);
//                    addPersonText.setVisibility(View.VISIBLE);
//
////                    addActionsFab.extend();
//                    isAllFABVisible = true;
//                } else {
//                    addAlarmFab.hide();
//                    addPersonFab.hide();
//                    addAlarmText.setVisibility(View.GONE);
//                    addPersonText.setVisibility(View.GONE);
//                    addActionsFab.shrink();
//                    isAllFABVisible = false;
//                }
                Intent intent = new Intent(getApplicationContext(), VistualAssistantActivity.class);
                Animatoo.animateFade(MainActivity.this);
                startActivity(intent);


            }
        });

        addPersonFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), VistualAssistantActivity.class);
                Animatoo.animateFade(MainActivity.this);
                startActivity(intent);
            }
        });

        addAlarmFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();
            }
        });

        setSupportActionBar(toolbar);
        getUserDetailForDrawer(user_id);
        checkFirstUser(user_id);
        // Set system UI visibility
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        sessionManager = new SessionManager(this);

        // Initialize navigation drawer components
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        View headerView = navigationView.getHeaderView(0);
        header_nav_username = headerView.findViewById(R.id.header_username);
        header_nav_email = headerView.findViewById(R.id.header_email);
        profile_image = headerView.findViewById(R.id.profile_image);

        // Display the "Up" button in the action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Location related entities
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(2000);



        // Set navigation item selection listener
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.home) {
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    Animatoo.animateSwipeLeft(MainActivity.this);
                    startActivity(intent);
                } else if (id == R.id.my_profile) {
                    Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
                    startActivity(intent);
                } else if (id == R.id.notification) {
                    Intent intent = new Intent(getApplicationContext(), NotificationActivity.class);
                    startActivity(intent);
                }  else if (id == R.id.my_favorite) {
                    Intent intent = new Intent(getApplicationContext(), ListFavorite.class);
                    startActivity(intent);
                } else if (id == R.id.feedback_concern) {
                    Intent intent = new Intent(getApplicationContext(), FeedbackAndConcernActivity.class);
                    startActivity(intent);
                }else if (id == R.id.about) {
                    Intent intent = new Intent(getApplicationContext(), AboutActivity.class);
                    startActivity(intent);
                }else if (id == R.id.logout) {
                    Toast.makeText(MainActivity.this, "Logout", Toast.LENGTH_SHORT).show();
                    logout();
                }
                return false;
            }
        });
        //calling categories function
        initCategories();
        //calling product function
        initLandmark();
        getCurrentLocation();
        initNewsEvents();

//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle("Virtual Assistant Guide");
//        View view = getLayoutInflater().inflate(R.layout.modal_virtual_assistant_guide, null);
//        //View dialog
//        builder.setView(view);
//        //Create dialog
//        dialog = builder.create();
//        virtualGuide = view.findViewById(R.id.description);
//        close_va = view.findViewById(R.id.close_va);
//        close_va.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                dialog.dismiss();
//            }
//        });
//
//        dialog.dismiss();


        //Search Municipality Name
        binding.searchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {

            }

            @Override
            public void onSearchConfirmed(CharSequence municipality_name) {
                RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
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
                                int municipality_id = municipality.getInt("municipality_id");
                                Intent intent = new Intent(MainActivity.this, MunicipalityDetailActivity.class);
                                intent.putExtra("municipality_id", municipality_id);
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

    private void checkFirstUser(String user_id) {
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String url = Constants.API_BASE_URL + "/users/check-first-user.php?user_id=" + user_id;
        StringRequest request = new StringRequest(Request.Method.GET, url, response -> {
            try {
                JSONObject object = new JSONObject(response);
                String status = object.getString("status");
                if ("success".equals(status)) {
                    JSONArray usersArray = object.getJSONArray("users"); // Use getJSONArray to get the array
                    if (usersArray.length() > 0) {
                        // Assuming you want the first item in the array
                        JSONObject user = usersArray.getJSONObject(0);
                        int is_first_time = user.getInt("is_first_time");
                        if (is_first_time==0){
                            Intent intent = new Intent(MainActivity.this, IntroActivity.class);
                            startActivity(intent);
                        }
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                if (isGPSEnabled()) {
                    getCurrentLocation();
                }else {
                    turnOnGPS();
                }
            }
        }
    }

    private void getCurrentLocation() {

//        progressDialog.setMessage("Finding your location...");
//        progressDialog.show();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if (isGPSEnabled()) {
                    LocationServices.getFusedLocationProviderClient(MainActivity.this)
                            .requestLocationUpdates(locationRequest, new LocationCallback() {
                                @Override
                                public void onLocationResult(@NonNull LocationResult locationResult) {
                                    super.onLocationResult(locationResult);
                                    LocationServices.getFusedLocationProviderClient(MainActivity.this)
                                            .removeLocationUpdates(this);
                                    if (locationResult != null && locationResult.getLocations().size() >0){
                                        int index = locationResult.getLocations().size() - 1;
                                        setLongitude = locationResult.getLocations().get(index).getLongitude();
                                        setLatitude = locationResult.getLocations().get(index).getLatitude();
                                        setCoordinate(setLongitude, setLatitude);

                                    }
                                }
                            }, Looper.getMainLooper());
                } else {
                    turnOnGPS();
                }
            } else {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
    }

    private void setCoordinate(double setLongitude, double setLatitude) {
        if (!String.valueOf(setLongitude).isEmpty() && !String.valueOf(setLatitude).isEmpty()){
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("setLongitude", String.valueOf(setLongitude));
            editor.putString("setLatitude", String.valueOf(setLatitude));
            editor.apply();
            //Make a loading for getting location
        }
    }

    private void turnOnGPS() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);
        Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(getApplicationContext())
                .checkLocationSettings(builder.build());

        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                    Toast.makeText(MainActivity.this, "GPS is already tured on", Toast.LENGTH_SHORT).show();

                } catch (ApiException e) {

                    switch (e.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:

                            try {
                                ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                                resolvableApiException.startResolutionForResult(MainActivity.this, 2);
                            } catch (IntentSender.SendIntentException ex) {
                                ex.printStackTrace();
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            //Device does not have location
                            break;
                    }
                }
            }
        });
    }

    private boolean isGPSEnabled() {
        LocationManager locationManager = null;
        boolean isEnabled = false;

        if (locationManager == null) {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        }

        isEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        return isEnabled;

    }
    private void getUserDetailForDrawer(String user_id) {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = Constants.API_BASE_URL+"/users/get-user-detail-for-drawer.php?user_id=" + user_id;
        StringRequest request = new StringRequest(Request.Method.GET, url, response -> {
            try {
                JSONObject object = new JSONObject(response);
                if (object.getString("status").equals("success")) {
                    JSONArray landmarksArray = object.getJSONArray("user_details");
                    if (landmarksArray.length() > 0) {
                        JSONObject landmark = landmarksArray.getJSONObject(0);
                        String user_name = landmark.getString("user_name");
                        String user_email = landmark.getString("user_email");
                        String user_photo = landmark.getString("user_photo");

                        header_nav_username.setText(user_name);
                        header_nav_email.setText(user_email);
                        Picasso.get().load(USER_IMAGE_URL+user_photo).into(profile_image);
                        Picasso.get().load(USER_IMAGE_URL+user_photo).into(logged_user_photo);
                    } else {
                        // Handle the case when no landmarks were found
                    }
                } else if (object.getString("status").equals("empty")) {
                    // Handle the case when no landmarks were found
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


    //---------------------- Replacing by default fragment on home activity




        //calling slider function
//        initSlider();
//    }


//    void initSlider(){
//        getRecentOffers();
//    }

    void initCategories(){
        categories = new ArrayList<>();
        categoryAdapter = new CategoryAdapter(this, categories);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
    }

    void getRecentLandmark() {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = Constants.API_BASE_URL+"/users/recent-landmark.php";

        StringRequest request = new StringRequest(Request.Method.GET, url, response -> {
            try {
                JSONObject object = new JSONObject(response);
                if(object.getString("status").equals("success")){
                    JSONArray productsArray = object.getJSONArray("landmarks");
                    for(int i =0; i< productsArray.length(); i++) {
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

//    void getRecentOffers() {
//        RequestQueue queue = Volley.newRequestQueue(this);
//
//        StringRequest request = new StringRequest(Request.Method.GET, Constants.GET_CATEGORIES_URL, response -> {
//            try {
//                JSONObject object = new JSONObject(response);
//                if (object.getString("status").equals("success")){
//                    JSONArray offerArray = object.getJSONArray("news_infos");
//                    for(int i = 0; i < offerArray.length(); i++){
//                        JSONObject childObj = offerArray.getJSONObject(i);
//                        binding.carousel.addData(
//                                new Carousel(
//                                        Constants.NEWS_IMAGE_URL + childObj.getString("image"),
//                                        childObj.getString("title")
//                                )
//                        );
//                    }
//                }
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//        }, error -> {
//        });
//        queue.add(request);
//    }

    //Product 1 (Convert to municipality)
    void initLandmark(){
        landmarks = new ArrayList<>();
        landmarkAdapter = new LandmarkAdapter(this, landmarks);
        getRecentLandmark();
        GridLayoutManager layoutManager = new GridLayoutManager(this, 1);
        binding.landmarkList.setLayoutManager(layoutManager);
        binding.landmarkList.setAdapter(landmarkAdapter);

        btnAdventure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), CategoryActivity.class);
                intent.putExtra("catId", 10);
                intent.putExtra("categoryName", "Adventure");
                startActivity(intent);
            }
        });

        btnCulturalHeritage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), CategoryActivity.class);
                intent.putExtra("catId", 11);
                intent.putExtra("categoryName", "Cultural Heritage");
                Animatoo.animateFade(MainActivity.this);
                startActivity(intent);
            }
        });

        btnProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), CategoryActivity.class);
                intent.putExtra("catId", 13);
                intent.putExtra("categoryName", "Product");
                Animatoo.animateFade(MainActivity.this);
                startActivity(intent);
            }
        });

        btnChurch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), CategoryActivity.class);
                intent.putExtra("catId", 12);
                intent.putExtra("categoryName", "Pilgrimage");
                Animatoo.animateFade(MainActivity.this);
                startActivity(intent);
            }
        });

        btnFamily.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), CategoryActivity.class);
                intent.putExtra("catId", 12);
                intent.putExtra("categoryName", "Family");
                Animatoo.animateFade(MainActivity.this);
                startActivity(intent);
            }
        });

        btnFarmAndNature.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), CategoryActivity.class);
                intent.putExtra("catId", 4);
                intent.putExtra("categoryName", "Farm and Nature");
                Animatoo.animateFade(MainActivity.this);
                startActivity(intent);
            }
        });
        btnIslandAndBeaches.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), CategoryActivity.class);
                intent.putExtra("catId", 3);
                intent.putExtra("categoryName", "Beach and Island");
                Animatoo.animateFade(MainActivity.this);
                startActivity(intent);
            }
        });

        btnRidgeAndReef.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), CategoryActivity.class);
                intent.putExtra("catId", 12);
                intent.putExtra("categoryName", "Ridge and Reef");
                Animatoo.animateFade(MainActivity.this);
                startActivity(intent);
            }
        });

        btnOther.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), CategoryActivity.class);
                intent.putExtra("catId", 12);
                intent.putExtra("categoryName", "Other");
                Animatoo.animateFade(MainActivity.this);
                startActivity(intent);
            }
        });
    }

    void initNewsEvents(){
        municipalities = new ArrayList<>();
        municipalityAdapter = new MunicipalityAdapter(this, municipalities);
        getMunicipalities();
        GridLayoutManager layoutManager = new GridLayoutManager(this, 1);
        binding.municipalityList.setLayoutManager(layoutManager);
        binding.municipalityList.setAdapter(municipalityAdapter);
    }

    void getMunicipalities() {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = Constants.API_BASE_URL+"/users/get-municipality-list.php";

        StringRequest request = new StringRequest(Request.Method.GET, url, response -> {
            try {
                JSONObject object = new JSONObject(response);
                if(object.getString("status").equals("success")){
                    JSONArray neArray = object.getJSONArray("municipalities");
                    for(int i =0; i< neArray.length(); i++) {
                        JSONObject childObj = neArray.getJSONObject(i);
                        Municipality municipality = new Municipality(
                                childObj.getString("municipality_name"),
                                childObj.getString("municipality_desc"),
                                Constants.MUNICIPALITY_IMAGE_URL + childObj.getString("municipality_image"),
                                childObj.getString("municipality_address"),
                                childObj.getString("municipality_lon"),
                                childObj.getString("municipality_lat"),
                                childObj.getInt("municipality_id")
                        );
                        municipalities.add(municipality);
                    }
                    municipalityAdapter.notifyDataSetChanged();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> { });

        queue.add(request);
    }

    public void logout() {
        sessionManager.editor.clear();
        sessionManager.editor.commit();
        Intent intent = new Intent(getApplicationContext(), EmailLoginActivity.class);
        startActivity(intent);
        finish();
        Animatoo.animateSplit(this);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START))
        {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        else
        {
            super.onBackPressed();
        }

    }
    public void goToNewsAndEventsActivity(View view) {
        Intent intent = new Intent(getApplicationContext(), NewsEventActivity.class);
        Animatoo.animateFade(MainActivity.this);
        startActivity(intent);
    }

    public void goToFiestaFestivalEvent(View view) {
        Intent intent = new Intent(getApplicationContext(), ListFiestaFestivalEvent.class);
        Animatoo.animateFade(MainActivity.this);
        startActivity(intent);
    }

    public void goToNearbyPlacesMenu(View view) {
        Intent intent = new Intent(getApplicationContext(), NearbyPlacesMenu.class);
        Animatoo.animateFade(MainActivity.this);
        startActivity(intent);
    }
    public void goToVirtualAssistantActivity(View view) {
        Intent intent = new Intent(getApplicationContext(), VistualAssistantActivity.class);
        Animatoo.animateFade(MainActivity.this);
        startActivity(intent);
    }
    public void goToFindDestination(View view) {
        Intent intent = new Intent(getApplicationContext(), FindDestinationActivity.class);
        Animatoo.animateSlideUp(MainActivity.this);
        startActivity(intent);
    }
}