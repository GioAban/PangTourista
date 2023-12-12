package pangtourista.project.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import pangtourista.project.R;

public class GetNearbyPlaces extends AppCompatActivity {
    SupportMapFragment supportMapFragment;
    GoogleMap map;
    FusedLocationProviderClient fusedLocationProviderClient;
    double currentLat = 0, currentLong = 0;
    String type;
    ProgressDialog dialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_get_nearby_places);

        dialog = new ProgressDialog(this);
        dialog.setTitle("Logging...");
        dialog.setMessage("Please wait! PangTourista finding nearby places...");
        dialog.show();
        dialog.setCanceledOnTouchOutside(false);

        type = getIntent().getStringExtra("place_type");

        supportMapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.google_map);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(GetNearbyPlaces.this
                , Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation();
        } else {
            ActivityCompat.requestPermissions(GetNearbyPlaces.this
                    , new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
        }
    }
    private void getCurrentLocation() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null){
                    currentLat = location.getLatitude();
                    currentLong = location.getLongitude();

                    supportMapFragment.getMapAsync(new OnMapReadyCallback() {
                        @Override
                        public void onMapReady(GoogleMap googleMap) {
                            map = googleMap;
                            // Add a marker to the user's current location
                            LatLng userLatLng = new LatLng(currentLat, currentLong);
                            MarkerOptions userMarkerOptions = new MarkerOptions()
                                    .position(userLatLng)
                                    .title("Your Location");

                            // Check if the map object is not null before adding the marker
                            if (map != null) {
                                try {
                                    map.addMarker(userMarkerOptions);
                                    // Move the camera to the user's location
                                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 12));
                                    String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json" +
                                            "?location=" + currentLat + "," + currentLong +
                                            "&radius=5000" +
                                            "&types=" + type +
                                            "&sensor=true" +
                                            "&key=" + getResources().getString(R.string.google_maps_key);
                                    new PlaceTask().execute(url);
                                } catch (Exception e){
                                    Toast.makeText(GetNearbyPlaces.this, e.toString(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });
                }
            }

        });
    }


    @Override
    @SuppressLint("MissingSuperCall")
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 44) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                getCurrentLocation();
            }
        }
    }

    private class PlaceTask extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... strings) {
            String data = null;
            try {
                data = downloadUrl(strings[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return data;
        }
        @Override
        protected void onPostExecute(String s) {
            new ParserTask().execute(s);
        }
    }

    private String downloadUrl(String string) throws IOException {
        URL url = new URL(string);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.connect();
        InputStream stream = connection.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        StringBuilder builder = new StringBuilder();
        String line = "";

        while ((line = reader.readLine()) != null){
            builder.append(line);
        }

        String data = builder.toString();
        reader.close();

        return data;
    }

    private class ParserTask extends AsyncTask<String, Integer, List<HashMap<String, String>>> {
        @Override
        protected List<HashMap<String, String>> doInBackground(String... strings) {
            JsonParser jsonParser = new JsonParser();
            List<HashMap<String, String>> mapList = new ArrayList<>(); // Initialize an empty list

            if (strings != null && strings.length > 0 && strings[0] != null) {
                try {
                    dialog.dismiss();
                    JSONObject object = new JSONObject(strings[0]);
                    mapList = jsonParser.parseResults(object);
                } catch (JSONException e) {
                    e.printStackTrace();
                    displayErrorToast("JSON Parsing Error: " + e.getMessage());
                }
            } else {
                // Handle the case where strings[0] is null or empty JSON data.
                displayErrorToast("Signal is Weak! please try again...");
            }
            return mapList;
        }

        private void displayErrorToast(final String message) {
            runOnUiThread(new Runnable() {
                public void run() {

                    // Display error toast immediately
                    displayErrorToast("Searching...");

                    // Delay the execution of getCurrentLocation by 2 seconds
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            getCurrentLocation(); // Call getCurrentLocation after a 2-second delay
                        }
                    }, 2000); // Delay of 2000 milliseconds (2 seconds)
                }

                private void displayErrorToast(String message) {
                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                }

            });
        }




        @Override
        protected void onPostExecute(List<HashMap<String, String>> hashMaps) {
            map.clear();
            for (int i = 0; i < hashMaps.size(); i++) {
                HashMap<String, String> hashMapList = hashMaps.get(i);
                double lat = Double.parseDouble(hashMapList.get("lat"));
                double lng = Double.parseDouble(hashMapList.get("lng")); // Corrected key

                String name = hashMapList.get("name");
                LatLng latLng = new LatLng(lat, lng);
                MarkerOptions options = new MarkerOptions();

                options.position(latLng);
                options.title(name);

                map.addMarker(options);

                LatLng userLatLng = new LatLng(currentLat, currentLong);
                BitmapDescriptor blueMarkerIcon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE);
                MarkerOptions userMarkerOptions = new MarkerOptions()
                        .position(userLatLng)
                        .title("Your Location")
                        .icon(blueMarkerIcon);
                map.addMarker(userMarkerOptions);
            }
        }
    }
}