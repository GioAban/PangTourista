package pangtourista.project.activities;

import static pangtourista.project.utils.Constants.LANDMARK_IMAGE_URL;
import static pangtourista.project.utils.Constants.USER_IMAGE_URL;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;

import java.lang.ref.WeakReference;
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
import pangtourista.project.R;
import pangtourista.project.databinding.ActivityMapServiceBinding;
import retrofit2.Call;
import retrofit2.Callback;

public class MapService extends AppCompatActivity {
    final long UPDATE_INTERVAL_IN_MILLISECONDS = 1000;
    final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = 500;
    private static final int DEFAULT_CAMERA_ZOOM = 8;
    private static final int CAMERA_ANIMATION_DURATION = 1000;
    LocationEngine locationEngine;
    private static NavNextbillionMap navNextbillionMap;
    private static boolean locationFound;
    Point currentLocation;
    MapView mapView;
    DirectionsRoute route;
    Point origin, destination;
    ActivityMapServiceBinding binding;
    TextView tVlandmarkName, tVcategory, tVlandmark_address;
    LinearLayout linearViewLandmark;
    TextToSpeech t1;

    ImageView landmarkImage;

    final NavigationLauncherLocationCallback callbackL = new NavigationLauncherLocationCallback(this);
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        Nextbillion.getInstance(getApplicationContext(), "b98e9dd2f9414231bae19340b76feff0");
        binding = ActivityMapServiceBinding.inflate(getLayoutInflater());
        setContentView(R.layout.activity_map_service);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String setLongitude = sharedPreferences.getString("setLongitude", "120.5606");
        String setLatitude = sharedPreferences.getString("setLatitude", "15.9806");

        tVlandmarkName = findViewById(R.id.landmark_name);
        tVcategory = findViewById(R.id.landmark_category);
        tVlandmark_address = findViewById(R.id.landmark_address);
        linearViewLandmark = findViewById(R.id.linearViewLandmark);
        landmarkImage = findViewById(R.id.landmarkImage);

        t1 = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if(i != TextToSpeech.ERROR)
                    t1.setLanguage(Locale.ENGLISH);
            }
        });


        //GPS Section
        checkPermissions();
        GPSUtils gpsUtils = new GPSUtils(this);
        gpsUtils.statusCheck();
        origin = Point.fromLngLat(Double.parseDouble(setLongitude),Double.parseDouble(setLatitude));
        Bundle bundle = getIntent().getExtras();

        String landmark_name = bundle.getString("landmark_name");
        String category = bundle.getString("category");
        String landmark_address = bundle.getString("address");
        String landmark_img_1 = bundle.getString("landmark_img_1");
        String lat = bundle.getString("lat");
        String lon = bundle.getString("long");

        tVlandmarkName.setText(landmark_name);
        tVcategory.setText(category);
        tVlandmark_address.setText(landmark_address);
        thingsToDoInPangasinan(landmark_name, landmark_address);
        Picasso.get().load(LANDMARK_IMAGE_URL+landmark_img_1).into(landmarkImage);


        linearViewLandmark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MapService.this, "View destination details", Toast.LENGTH_SHORT).show();
            }
        });

        Glide.with(this).load(LANDMARK_IMAGE_URL + landmark_img_1).into(binding.landmarkImage);

        destination = Point.fromLngLat(Double.parseDouble(lon) , Double.parseDouble(lat));
        mapView = findViewById(R.id.mapViewService);
        mapView.onCreate(savedInstanceState);
        Button button = findViewById(R.id.btnStartNavService);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavLauncherConfig.Builder  configBuilder = NavLauncherConfig.builder(route);
                NavigationLauncher.startNavigation(MapService.this, configBuilder.build());
            }
        });

        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull NextbillionMap nextbillionMap) {
                String styleUri = "https://api.nextbillion.io/maps/streets/style.json?key="
                        + Nextbillion.getAccessKey();
                nextbillionMap.setStyle(new Style.Builder().fromUri(styleUri));
                nextbillionMap.getStyle(new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {
                        navNextbillionMap = new NavNextbillionMap(mapView, nextbillionMap);
                        navNextbillionMap.updateLocationLayerRenderMode(RenderMode.COMPASS);
                        initializeLocationEngine();
                        animateCamera(new LatLng(origin.latitude(), origin.longitude()));
                        navNextbillionMap.addMarker(getApplicationContext(), destination);
                        fetchRoute();


                    }
                });
            }
        });
    }

    private void thingsToDoInPangasinan(String landmark_name, String landmark_address) {
        try {
            t1 = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    if (status == TextToSpeech.SUCCESS) {
                        int result = t1.setLanguage(Locale.ENGLISH);
                        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                            Log.e("TTS", "Language not supported.");
                        } else {
                            // Speak the text
                            String speak_this = landmark_name + " is located in " + landmark_address;
                            t1.speak(speak_this, TextToSpeech.QUEUE_FLUSH, null);
                        }
                    } else {
                        Log.e("TTS", "Initialization failed.");
                    }
                }
            });

        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void checkPermissions() {
        if (ContextCompat.checkSelfPermission(MapService.this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MapService.this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(MapService.this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            } else {
                ActivityCompat.requestPermissions(MapService.this,
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
                        MapService.this.route = route;
                        navNextbillionMap.drawRoute(route);

                    }

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

        private final WeakReference<MapService> activityWeakReference;

        NavigationLauncherLocationCallback(MapService activity) {
            this.activityWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void onSuccess(LocationEngineResult result) {
            MapService activity = activityWeakReference.get();
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

}
