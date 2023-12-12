package pangtourista.project.activities;

import static pangtourista.project.utils.Constants.LANDMARK_IMAGE_URL;
import static pangtourista.project.utils.Constants.MUNICIPALITY_IMAGE_URL;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.Display;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Locale;

import ai.nextbillion.kits.directions.models.DirectionsResponse;
import ai.nextbillion.kits.directions.models.DirectionsRoute;
import ai.nextbillion.kits.geojson.Point;
import ai.nextbillion.maps.Nextbillion;
import ai.nextbillion.maps.camera.CameraUpdate;
import ai.nextbillion.maps.camera.CameraUpdateFactory;
import ai.nextbillion.maps.core.MapView;
import ai.nextbillion.maps.core.NextbillionMap;
import ai.nextbillion.maps.core.OnMapReadyCallback;
import ai.nextbillion.maps.core.Style;
import ai.nextbillion.maps.geometry.LatLng;
import ai.nextbillion.maps.location.engine.LocationEngine;
import ai.nextbillion.maps.location.engine.LocationEngineCallback;
import ai.nextbillion.maps.location.engine.LocationEngineProvider;
import ai.nextbillion.maps.location.engine.LocationEngineRequest;
import ai.nextbillion.maps.location.engine.LocationEngineResult;
import ai.nextbillion.maps.location.modes.RenderMode;
import ai.nextbillion.navigation.ui.NBNavigation;
import ai.nextbillion.navigation.ui.NavLauncherConfig;
import ai.nextbillion.navigation.ui.NavigationLauncher;
import ai.nextbillion.navigation.ui.camera.CameraUpdateMode;
import ai.nextbillion.navigation.ui.camera.NavigationCameraUpdate;
import ai.nextbillion.navigation.ui.map.NavNextbillionMap;

import pangtourista.project.Adapters.MunicipalityLandmarkAdapter;
import pangtourista.project.Models.Event;
import pangtourista.project.Models.Landmark;
import pangtourista.project.R;
import pangtourista.project.databinding.ActivityMunicipalityDetailBinding;
import pangtourista.project.utils.Constants;
import retrofit2.Call;
import retrofit2.Callback;

public class MunicipalityDetailActivity extends AppCompatActivity {

    final long UPDATE_INTERVAL_IN_MILLISECONDS = 1000;
    final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = 500;
    private static final int DEFAULT_CAMERA_ZOOM = 7;
    private static final int CAMERA_ANIMATION_DURATION = 1000;
    LocationEngine locationEngine;
    private static NavNextbillionMap navNextbillionMap;
    private static boolean locationFound;
    Point currentLocation;
    MapView mapViewMunicipality;
    DirectionsRoute route;
    Point origin, destination;

    ActivityMunicipalityDetailBinding binding;
    TextView municipalityName, municipalityAddress, municipalityDescription, mainProduct, fiestaFestivalEvents, seeMunicipality, generatingRouteText;

    Button button, btnSeeMunicipality;
    ProgressBar generatingRouteIcon;
    TextToSpeech t1;
    String speak_history, speak_product, speak_festival;
    private Toolbar toolbar;
    AlertDialog dialog;
    TextView municipality_main_product;
    Button close_va;
    LinearLayout main_product_layout, festival_layout;

    final NavigationLauncherLocationCallback callbackL = new NavigationLauncherLocationCallback(this);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Nextbillion.getInstance(getApplicationContext(), "b98e9dd2f9414231bae19340b76feff0");
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        binding = ActivityMunicipalityDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        toolbar = findViewById(R.id.detail_municipality);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Municipality/City");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        int whiteColor = ContextCompat.getColor(this, android.R.color.white);
        toolbar.setTitleTextColor(whiteColor);
        setNavigationIconColor(toolbar, whiteColor);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        int municipality_id = getIntent().getIntExtra("municipality_id",0);
        String setLongitude = sharedPreferences.getString("setLongitude", "120.5606");
        String setLatitude = sharedPreferences.getString("setLatitude", "15.9806");
        speak_history = getIntent().getStringExtra("speak_history");
        speak_product = getIntent().getStringExtra("speak_product");
        speak_festival = getIntent().getStringExtra("speak_festival");

        municipalityName = findViewById(R.id.municipalityName);
        municipalityAddress = findViewById(R.id.municipalityAddress);
        municipalityDescription = findViewById(R.id.municipalityDescription);
        mainProduct = findViewById(R.id.mainProduct);
        fiestaFestivalEvents = findViewById(R.id.fiestaFestivalEvent);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.modal_for_municipality_information, null);
        builder.setTitle("PANGTOURISTA VIRTUAL ASSISTANT...");
        builder.setView(view);
        dialog = builder.create();

        municipality_main_product = view.findViewById(R.id.modal_description);
        close_va = view.findViewById(R.id.modal_close_va);
        close_va.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                t1.stop();
                dialog.dismiss();
            }
        });

        main_product_layout = findViewById(R.id.main_product_layout);
        festival_layout = findViewById(R.id.festival_layout);

        initializeMunicipalityDetail(municipality_id);
        initializeMunicipalityEvent(municipality_id);

        //GPS Section
        checkPermissions();
        GPSUtils gpsUtils = new GPSUtils(this);
        gpsUtils.statusCheck();

        origin = Point.fromLngLat(Double.parseDouble(setLongitude),Double.parseDouble(setLatitude));
        mapViewMunicipality = findViewById(R.id.municipalityMapView);
        mapViewMunicipality.onCreate(savedInstanceState);
        btnSeeMunicipality = findViewById(R.id.btnSeeMunicipality);
        generatingRouteIcon = findViewById(R.id.generatingRouteIcon);
        generatingRouteText = findViewById(R.id.generatingRouteText);
        button = findViewById(R.id.btnStartNav);

        // Start speak section
        t1 = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if(i != TextToSpeech.ERROR)
                    t1.setLanguage(Locale.ENGLISH);
            }
        });
        // End Speak section

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavLauncherConfig.Builder  configBuilder = NavLauncherConfig.builder(route);
                NavigationLauncher.startNavigation(MunicipalityDetailActivity.this, configBuilder.build());
            }
        });

        btnSeeMunicipality.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SeeMunicipalityActivity.class);
                intent.putExtra("municipality_id", municipality_id);
                startActivity(intent);
            }
        });

        mapViewMunicipality.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull NextbillionMap nextbillionMap) {
                String styleUri = "https://api.nextbillion.io/maps/streets/style.json?key="
                        + Nextbillion.getAccessKey();
                nextbillionMap.setStyle(new Style.Builder().fromUri(styleUri));
                nextbillionMap.getStyle(new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {
                        navNextbillionMap = new NavNextbillionMap(mapViewMunicipality, nextbillionMap);
                        navNextbillionMap.updateLocationLayerRenderMode(RenderMode.COMPASS);
                        initializeLocationEngine();
                        animateCamera(new LatLng(origin.latitude(), origin.longitude()));
                        try {
                            navNextbillionMap.addMarker(getApplicationContext(), destination);

                        } catch (Exception e){
//                            Toast.makeText(MunicipalityDetailActivity.this, "Something went wrong, Try again later", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

    private void initializeMunicipalityDetail(int municipality_id) {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = Constants.API_BASE_URL + "/users/get-municipality-detail.php?id=" + municipality_id;
        StringRequest request = new StringRequest(Request.Method.GET, url, response -> {
            try {
                JSONObject object = new JSONObject(response);
                String status = object.getString("status");
                if ("success".equals(status)) {
                    JSONObject municipality = object.getJSONObject("municipalities");
                    String municipality_image = municipality.getString("municipality_image");
                    String municipality_name = municipality.getString("municipality_name");
                    String municipality_address = municipality.getString("municipality_address");
                    String municipality_desc = municipality.getString("municipality_desc");
                    String main_product = municipality.getString("main_product");
                    String municipality_logo = municipality.getString("municipality_logo");
                    String municipality_lon = municipality.getString("municipality_lon");
                    String municipality_lat = municipality.getString("municipality_lat");

                    municipalityName.setText(municipality_name);
                    String seeMunicipality = "See " + municipality_name;
                    btnSeeMunicipality.setText(seeMunicipality);
                    municipalityAddress.setText(municipality_address);
                    Spanned municipality_desc_html = Html.fromHtml(municipality_desc, Html.FROM_HTML_MODE_LEGACY);
                    municipalityDescription.setText(municipality_desc_html);

                    if (main_product.isEmpty()){
                        main_product_layout.setVisibility(View.GONE);
                    }
                    Spanned main_product_html = Html.fromHtml(main_product, Html.FROM_HTML_MODE_LEGACY);
                    mainProduct.setText(main_product_html);

                    if (!municipality_lon.isEmpty()||!municipality_lat.isEmpty()){
                        destination = Point.fromLngLat(Double.parseDouble(municipality_lon), Double.parseDouble(municipality_lat));
                        fetchRoute();
                    }

                    try {
                        if (speak_history.equals("true")) {
                            t1 = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
                                @Override
                                public void onInit(int status) {
                                    if (status == TextToSpeech.SUCCESS) {
                                        int result = t1.setLanguage(Locale.ENGLISH);
                                        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                                            Log.e("TTS", "Language not supported.");
                                        } else {
                                            // Speak the text
                                            Spanned municipality_desc_html = Html.fromHtml(municipality_desc, Html.FROM_HTML_MODE_LEGACY);
                                            t1.speak(municipality_desc_html.toString(), TextToSpeech.QUEUE_FLUSH, null);
                                        }
                                    } else {
                                        Log.e("TTS", "Initialization failed.");
                                    }
                                }
                            });
                        }
                    } catch (Exception e){
                        e.printStackTrace();
                    }

                    try {
                       if (speak_product.equals("true")) {
                            t1 = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
                                @Override
                                public void onInit(int status) {
                                    if (status == TextToSpeech.SUCCESS) {
                                        int result = t1.setLanguage(Locale.ENGLISH);
                                        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                                            Log.e("TTS", "Language not supported.");
                                        } else {
                                            // Speak the text
                                            Spanned main_product_html = Html.fromHtml(main_product, Html.FROM_HTML_MODE_LEGACY);
                                            t1.speak(main_product_html.toString(), TextToSpeech.QUEUE_FLUSH, null);
                                            municipality_main_product.setText(main_product_html);
                                            dialog.show();

                                        }
                                    } else {
                                        Log.e("TTS", "Initialization failed.");
                                    }
                                }
                            });
                        }
                    } catch (Exception e){
                        e.printStackTrace();
                    }

                    if (!municipality_image.isEmpty()) {
                        Glide.with(this).load(MUNICIPALITY_IMAGE_URL + municipality_image).into(binding.municipalityImage);
                    }
                    if (!municipality_logo.isEmpty()) {
                        binding.municipalityLogo.setVisibility(View.VISIBLE);
                        Glide.with(this).load(MUNICIPALITY_IMAGE_URL + municipality_logo).into(binding.municipalityLogo);
                    }
                } else if ("empty".equals(status)) {
                    // Handle the case when no municipalities were found
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

    String eventInfo = "";
    private void initializeMunicipalityEvent(int municipality_id) {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = Constants.API_BASE_URL + "/users/get-specific-event.php?municipality_id=" + municipality_id;
        StringRequest request = new StringRequest(Request.Method.GET, url, response -> {
            try {
                JSONObject object = new JSONObject(response);
                if (object.getString("status").equals("success")) {
                    JSONArray productsArray = object.getJSONArray("events");
                    for (int i = 0; i < productsArray.length(); i++) {
                        JSONObject childObj = productsArray.getJSONObject(i);
                        Event event = new Event(
                                childObj.getString("event_title"),
                                childObj.getString("event_date_start"),
                                childObj.getString("event_date_end"),
                                childObj.getString("description")
                        );
                        Spanned getDescription = Html.fromHtml(event.getDescription(), Html.FROM_HTML_MODE_LEGACY);
                        eventInfo = eventInfo + event.getEvent_title() +
                                " starts on " + event.getEvent_date_start() + " and ends on " + event.getEvent_date_end() + ".\n" + getDescription;
                    }
                    fiestaFestivalEvents.setText(eventInfo);
                    displayFestivalDialog(eventInfo);
                    eventInfo = "";
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> {
            // Handle error here, if needed
        });
        queue.add(request);
    }
    private void displayFestivalDialog(String eventInfo) {
            try {
                if (speak_festival.equals("true")) {
                    t1 = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
                        @Override
                        public void onInit(int status) {
                            if (status == TextToSpeech.SUCCESS) {
                                int result = t1.setLanguage(Locale.ENGLISH);
                                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                                    Log.e("TTS", "Language not supported.");
                                } else {
                                    Spanned spannedTextEventInfo = Html.fromHtml(eventInfo, Html.FROM_HTML_MODE_LEGACY);
                                    t1.speak(spannedTextEventInfo.toString(), TextToSpeech.QUEUE_FLUSH, null);
                                    dialog.show();
                                    municipality_main_product.setText(spannedTextEventInfo);
                                }
                            } else {
                                Log.e("TTS", "Initialization failed.");
                            }
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
    }

    public void checkPermissions() {
        if (ContextCompat.checkSelfPermission(MunicipalityDetailActivity.this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MunicipalityDetailActivity.this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(MunicipalityDetailActivity.this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            } else {
                ActivityCompat.requestPermissions(MunicipalityDetailActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
    }

    public void fetchRoute(){
        NBNavigation.fetchRoute(origin, destination, new Callback<DirectionsResponse>() {
            @Override
            public void onResponse(Call<DirectionsResponse> call, retrofit2.Response<DirectionsResponse> response) {
                if (response.isSuccessful()){
                    DirectionsResponse directionsResponse = response.body();
                    DirectionsRoute route = directionsResponse.routes().get(0);
                    if (route.distance() > 25d){
                        MunicipalityDetailActivity.this.route = route;
                        navNextbillionMap.drawRoute(route);
                        button.setVisibility(View.VISIBLE);
                        generatingRouteIcon.setVisibility(View.GONE);
                        generatingRouteText.setVisibility(View.GONE);
                    }else
                        Snackbar.make(mapViewMunicipality, "Select longer route", Snackbar.LENGTH_LONG).show();
                }
            }
            @Override
            public void onFailure(Call<DirectionsResponse> call, Throwable t) {

            }
        });
    }


    @NonNull
    private LocationEngineRequest buildEngineRequest() {
        return new LocationEngineRequest.Builder(UPDATE_INTERVAL_IN_MILLISECONDS).
                setFastestInterval(UPDATE_INTERVAL_IN_MILLISECONDS).
                setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY).build();
    }

    @SuppressWarnings({"MissingPermission"})
    private void initializeLocationEngine() {
        locationEngine = LocationEngineProvider.getBestLocationEngine(this);
        LocationEngineRequest request = buildEngineRequest();
        locationEngine.requestLocationUpdates(request, callbackL, null);
        locationEngine.getLastLocation(callbackL);
    }

    private static class NavigationLauncherLocationCallback
            implements LocationEngineCallback<LocationEngineResult> {

        private final WeakReference<MunicipalityDetailActivity> activityWeakReference;

        NavigationLauncherLocationCallback(MunicipalityDetailActivity activity) {
            this.activityWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void onSuccess(LocationEngineResult result) {
            MunicipalityDetailActivity activity = activityWeakReference.get();
            if (activity != null) {
                Location location = result.getLastLocation();
                if (location == null) {
                    return;
                }
                activity.updateCurrentLocation(Point.fromLngLat(location.getLongitude(), location.getLatitude()));
                activity.onLocationFound(location);
            }
        }

        @Override
        public void onFailure(@NonNull Exception exception) {
            exception.printStackTrace();
        }
    }

    void updateCurrentLocation(Point currentLocation){
        this.currentLocation = currentLocation;
    }

    void onLocationFound(Location location){
        navNextbillionMap.updateLocation(location);
        if (!locationFound){
            animateCamera(new LatLng(location.getLatitude(), location.getLongitude()));
            locationFound = true;
        }
    }

    private static void animateCamera(LatLng point){
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(point, DEFAULT_CAMERA_ZOOM);
        NavigationCameraUpdate navigationCameraUpdate = new NavigationCameraUpdate(cameraUpdate);
        navigationCameraUpdate.setMode(CameraUpdateMode.OVERRIDE);
        navNextbillionMap.retrieveCamera().update(navigationCameraUpdate, CAMERA_ANIMATION_DURATION);
    }

    @Override
    protected void onPause() {
        if (t1 != null) {
            t1.stop();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (t1 != null) {
            t1.shutdown();
        }
        super.onDestroy();
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