package pangtourista.project.activities;

import static pangtourista.project.utils.Constants.LANDMARK_IMAGE_URL;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.bumptech.glide.Glide;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.android.material.chip.Chip;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

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

import pangtourista.project.EmailLoginRegister.EmailLoginActivity;

import pangtourista.project.R;
import pangtourista.project.Sessions.SessionManager;
import pangtourista.project.databinding.ActivityLandmarkDetailBinding;
import pangtourista.project.utils.Constants;
import retrofit2.Call;
import retrofit2.Callback;

public class LandmarkDetailActivity extends AppCompatActivity {

    ActivityLandmarkDetailBinding binding;
    TextView landmarkDescription, landmarkName, address, description2, description3, description4, description5;
    SessionManager sessionManager;
    String loggedInUserId;
    ImageButton videoBtn, saveBtn, unSaveBtn;
    TextInputEditText input_comment;
    Button chipActiveStatus, chipInActiveStatus;
    TextView commentBtn;


    //View Location
    final long UPDATE_INTERVAL_IN_MILLISECONDS = 1000;
    final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = 500;
    private static final int DEFAULT_CAMERA_ZOOM = 7;
    private static final int CAMERA_ANIMATION_DURATION = 1000;
    LocationEngine locationEngine;
    private static NavNextbillionMap navNextbillionMap;
    private static boolean locationFound;
    Point currentLocation;
    MapView mapView;
    DirectionsRoute route;
    Point origin, destination;
    Button button;

    AlertDialog dialog, videoDialog;
    CardView landmarkImageCard2, landmarkImageCard3, landmarkImageCard4, landmarkImageCard5;
    ImageButton btnMoreInfo;
    TextView operational_hour, website_link, best_time_to_visit, tags_keyword, things_to_do, how_to_get_there;

    final NavigationLauncherLocationCallback callbackL = new NavigationLauncherLocationCallback(this);
    String speak_description;
    TextToSpeech t1;

    VideoView videoView;
    MediaController mediaController;

    Toolbar toolbar;
    TextView avrRating, numberOfReview;
    RatingBar averageRatingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Nextbillion.getInstance(getApplicationContext(), "b98e9dd2f9414231bae19340b76feff0");
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        binding = ActivityLandmarkDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        sessionManager = new SessionManager(this);
        loggedInUserId = sessionManager.getUserDetail().get("USER_ID");

        videoBtn = findViewById(R.id.videoBtn);

        toolbar = findViewById(R.id.landmark_detail);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Landmark Information");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        int whiteColor = ContextCompat.getColor(this, android.R.color.white);
        toolbar.setTitleTextColor(whiteColor);
        setNavigationIconColor(toolbar, whiteColor);


        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String setLongitude = sharedPreferences.getString("setLongitude", "120.5606");
        String setLatitude = sharedPreferences.getString("setLatitude", "15.9806");
        speak_description = getIntent().getStringExtra("speak_description");

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("More information");
        View view = getLayoutInflater().inflate(R.layout.landmark_more_information, null);
        //View dialog
        builder.setView(view);
        //Create dialog
        dialog = builder.create();
        operational_hour = view.findViewById(R.id.operational_hour);
        website_link = view.findViewById(R.id.website_link);
        best_time_to_visit = view.findViewById(R.id.best_time_to_visit);
        tags_keyword = view.findViewById(R.id.tags_keyword);
        things_to_do = view.findViewById(R.id.things_to_do);
        how_to_get_there = view.findViewById(R.id.how_to_get_there);
        dialog.dismiss();

        //Video Section
        AlertDialog.Builder videoBuilder = new AlertDialog.Builder(this);
        videoBuilder.setTitle("Video...");
        View video_View = getLayoutInflater().inflate(R.layout.item_video, null);
        //View dialog
        videoBuilder.setView(video_View);
        //Create dialog
        videoDialog = videoBuilder.create();

        videoView = (VideoView) video_View.findViewById(R.id.videoView);
        mediaController = new MediaController(this);
        videoDialog.dismiss();

        videoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                videoDialog.show();
                if (videoView.isPlaying()) {
                    videoView.seekTo(0); // Rewind the video to the beginning
                } else {
                    videoView.start(); // Start playing if it's not already playing
                }
            }
        });

        // Start speak section
        t1 = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if(i != TextToSpeech.ERROR)
                    t1.setLanguage(Locale.ENGLISH);
            }
        });
        // End Speak section

        landmarkName = findViewById(R.id.landmarkName);
        landmarkDescription = findViewById(R.id.landmarkDescription);
        address = findViewById(R.id.landmarkAddress);

        btnMoreInfo = findViewById(R.id.btnMoreInfo);

        landmarkImageCard2 = findViewById(R.id.landmarkImageCard2);
        landmarkImageCard3 = findViewById(R.id.landmarkImageCard3);
        landmarkImageCard4 = findViewById(R.id.landmarkImageCard4);
        landmarkImageCard5 = findViewById(R.id.landmarkImageCard5);

        description2 = findViewById(R.id.description_2);
        description3 = findViewById(R.id.description_3);
        description4 = findViewById(R.id.description_4);
        description5 = findViewById(R.id.description_5);

        //Comment section
        chipActiveStatus = findViewById(R.id.chipActiveStatus);
        chipInActiveStatus = findViewById(R.id.chipInActiveStatus);
        commentBtn = findViewById(R.id.commentBtn);
        videoBtn = findViewById(R.id.videoBtn);
        saveBtn = findViewById(R.id.saveBtn);
        unSaveBtn = findViewById(R.id.unSaveBtn);
        avrRating = findViewById(R.id.avrRating);
        numberOfReview = findViewById(R.id.numberOfReview);
        averageRatingBar = findViewById(R.id.averageRatingBar);

        int landmark_id = getIntent().getIntExtra("id",0);

        //Calling the function for getting informations
        getLandmarkDetails(landmark_id);
        getLandmarkReview(landmark_id);

        isLandmarkAdded(landmark_id, loggedInUserId);
        addView(landmark_id, loggedInUserId);


        commentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), LandmarkCommentSection.class);
                intent.putExtra("id", landmark_id);
                Animatoo.animateSplit(LandmarkDetailActivity.this);
                startActivity(intent);
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveBtn.setVisibility(View.GONE);
                saveToAccount(landmark_id, loggedInUserId);
                Intent intent = new Intent(getApplicationContext(), ListFavorite.class);
                Animatoo.animateFade(LandmarkDetailActivity.this);
                startActivity(intent);

            }
        });

        unSaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unSaveBtn.setVisibility(View.GONE);
                unSaveToAccount(landmark_id, loggedInUserId);
                Intent intent = new Intent(getApplicationContext(), ListFavorite.class);
                Animatoo.animateFade(LandmarkDetailActivity.this);
                startActivity(intent);
            }
        });

        btnMoreInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();
            }
        });



        checkPermissions();
        GPSUtils gpsUtils = new GPSUtils(this);
        gpsUtils.statusCheck();
        origin = Point.fromLngLat(Double.parseDouble(setLongitude),Double.parseDouble(setLatitude));
        Bundle bundle = getIntent().getExtras();
        String lat = bundle.getString("lat");
        String lon = bundle.getString("long");


        destination = Point.fromLngLat(Double.parseDouble(lon), Double.parseDouble(lat));
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        button = findViewById(R.id.btnStartNav);
        button.setVisibility(View.INVISIBLE);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavLauncherConfig.Builder  configBuilder = NavLauncherConfig.builder(route);
                NavigationLauncher.startNavigation(LandmarkDetailActivity.this, configBuilder.build());
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
                        try {
                            navNextbillionMap.addMarker(getApplicationContext(), destination);

                        } catch (Exception e){
                            Toast.makeText(LandmarkDetailActivity.this, "Something went wrong, Try again later", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

    }

    private void getLandmarkReview(int landmark_id) {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = Constants.API_BASE_URL + "/users/get-landmark-review.php?id=" + landmark_id;
        StringRequest request = new StringRequest(Request.Method.GET, url, response -> {
            try {
                JSONObject object = new JSONObject(response);
                if (object.getString("status").equals("success")) {
                    // Check if "average_rating" key is present in the JSON response
                    if (object.has("average_rating")) {
                        double averageRating = object.getDouble("average_rating");
                        int numReviews = object.getInt("num_reviews");
                        String averageRatingString = String.valueOf(averageRating);
                        String numReviewsString = String.valueOf(numReviews);
                        // Assuming averageRatingString is a String variable
                        String truncatedRating = averageRatingString.substring(0, Math.min(averageRatingString.length(), 3));
                        avrRating.setText(truncatedRating);

                        numberOfReview.setText(numReviewsString);
                        averageRatingBar.isIndicator();
                        if (Float.parseFloat(averageRatingString) >= 4.5) {
                            averageRatingBar.setRating(5);
                        } else if (Float.parseFloat(averageRatingString) >= 3.5) {
                            averageRatingBar.setRating(4);
                        } else if (Float.parseFloat(averageRatingString) >= 2.5) {
                            averageRatingBar.setRating(3);
                        } else if (Float.parseFloat(averageRatingString) >= 1.5) {
                            averageRatingBar.setRating(2);  // Corrected this line to set the rating to 2
                        } else if (Float.parseFloat(averageRatingString) >= 1) {
                            averageRatingBar.setRating(1);
                        } else {
                            averageRatingBar.setRating(0);
                        }
                    } else {
                        // Handle the case when "average_rating" key is not present
                        // This might happen if there are no reviews
                    }
                } else if (object.getString("status").equals("empty")) {
                    // Handle the case when no landmarks were found
                } else {
                    // Handle the error case
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> {
            // Handle error response
        });

        queue.add(request);
    }


    private void addView(int landmark_id, String loggedInUserId) {
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String url = Constants.API_BASE_URL+"/users/add-landmark-view.php";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
//                            progressBar.setVisibility(View.GONE);
                    if (response.equals("success"))
                    {

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
//                progressBar.setVisibility(View.GONE);
                error.printStackTrace();
            }
        }){
            protected Map<String, String> getParams(){
                Map<String, String> paramV = new HashMap<>();
                paramV.put("user_id", loggedInUserId);
                paramV.put("landmark_id", ""+landmark_id);
                return paramV;
            }
        };
        stringRequest.setRetryPolicy(new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return 30000;
            }

            @Override
            public int getCurrentRetryCount() {
                return 30000;
            }

            @Override
            public void retry(VolleyError error) throws VolleyError {

            }
        });
        queue.add(stringRequest);
        //Api End
    }

    private void unSaveToAccount(int landmark_id, String loggedInUserId) {
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String url = Constants.API_BASE_URL+"/users/unSave-favorite.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
//                            progressBar.setVisibility(View.GONE);
                    if (response.equals("success"))
                    {
                        isLandmarkAdded(landmark_id, loggedInUserId);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
//                progressBar.setVisibility(View.GONE);
                error.printStackTrace();
            }
        }){
            protected Map<String, String> getParams(){
                Map<String, String> paramV = new HashMap<>();
                paramV.put("user_id", loggedInUserId);
                paramV.put("landmark_id", String.valueOf(landmark_id));
                return paramV;
            }
        };
        stringRequest.setRetryPolicy(new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return 30000;
            }

            @Override
            public int getCurrentRetryCount() {
                return 30000;
            }

            @Override
            public void retry(VolleyError error) throws VolleyError {

            }
        });
        queue.add(stringRequest);
        //Api End
    }

    private void isLandmarkAdded(int landmark_id, String loggedInUserId) {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = Constants.API_BASE_URL + "/users/get-added-landmark.php?landmark_id=" + landmark_id + "&user_id=" + loggedInUserId;
        StringRequest request = new StringRequest(Request.Method.GET, url, response -> {
            try {
                JSONObject object = new JSONObject(response);
                String status = object.getString("status");
                if ("success".equals(status)) {
                    JSONArray isLandmarkAddedArray = object.getJSONArray("is_landmark_added");
                    if (isLandmarkAddedArray.length() > 0) {
                        unSaveBtn.setVisibility(View.VISIBLE);
                        saveBtn.setVisibility(View.GONE);
                    } else {
                        saveBtn.setVisibility(View.VISIBLE);
                        unSaveBtn.setVisibility(View.GONE);
                    }
                } else if ("empty".equals(status)) {
                    saveBtn.setVisibility(View.VISIBLE);
                } else {
                    // Handle other error cases
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> {
            // Handle error response (e.g., network error)
            error.printStackTrace();
        });
        queue.add(request);
    }


    private void saveToAccount(int id, String loggedInUserId) {
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String url = Constants.API_BASE_URL+"/users/add-to-favorite.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
//                            progressBar.setVisibility(View.GONE);
                    if (response.equals("success"))
                    {
                        isLandmarkAdded(id, loggedInUserId);
                    }
                    else {
                        Toast.makeText(LandmarkDetailActivity.this, response, Toast.LENGTH_SHORT).show();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
//                progressBar.setVisibility(View.GONE);
                error.printStackTrace();
            }
        }){
            protected Map<String, String> getParams(){
                Map<String, String> paramV = new HashMap<>();
                paramV.put("user_id", loggedInUserId);
                paramV.put("landmark_id", ""+id);
                return paramV;
            }
        };
        stringRequest.setRetryPolicy(new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return 30000;
            }

            @Override
            public int getCurrentRetryCount() {
                return 30000;
            }

            @Override
            public void retry(VolleyError error) throws VolleyError {

            }
        });
        queue.add(stringRequest);
        //Api End
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

    void getLandmarkDetails(int id) {
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setTitle("Loading...");
        dialog.setMessage("Please wait...");
        dialog.show();

        RequestQueue queue = Volley.newRequestQueue(this);
        String url = Constants.API_BASE_URL+"/users/get-landmark-detail.php?id=" + id;
        @SuppressLint("ResourceAsColor") StringRequest request = new StringRequest(Request.Method.GET, url, response -> {
            try {
                JSONObject object = new JSONObject(response);
                if (object.getString("status").equals("success")) {
                    JSONArray landmarksArray = object.getJSONArray("landmarks");
                    if (landmarksArray.length() > 0) {
                        JSONObject landmark = landmarksArray.getJSONObject(0);
                        dialog.dismiss();
                        String name = landmark.getString("landmark_name");
                        String description = landmark.getString("description_1");
                        String description_2 = landmark.getString("description_2");
                        String description_3 = landmark.getString("description_3");
                        String description_4 = landmark.getString("description_4");
                        String description_5 = landmark.getString("description_5");
                        String landmarkAddress = landmark.getString("address");
                        String longitude = landmark.getString("longitude");
                        String latitude = landmark.getString("latitude");
                        String landmark_img_1 = landmark.getString("landmark_img_1");
                        String landmark_img_2 = landmark.getString("landmark_img_2");
                        String landmark_img_3 = landmark.getString("landmark_img_3");
                        String landmark_img_4 = landmark.getString("landmark_img_4");
                        String landmark_img_5 = landmark.getString("landmark_img_5");
                        String destinationVideo = landmark.getString("destinationVideo");

                        //More information
                        String  is_archive = landmark.getString("is_archive");
                        String 	operational_hours = landmark.getString("operational_hours");
                        String 	website_link = landmark.getString("website_link");
                        String 	how_to_get_there = landmark.getString("how_to_get_there");
                        String 	best_time_to_visit = landmark.getString("best_time_to_visit");
                        String 	things_to_do = landmark.getString("things_to_do");
                        String 	tags_key_word = landmark.getString("tags_key_word");

                        if (!destinationVideo.equals("")) {
                            videoBtn.setVisibility(View.VISIBLE);
                            videoView.setVideoURI(Uri.parse(Constants.API_BASE_URL + "/uploads/video/"+destinationVideo));
                            videoView.setMediaController(mediaController);
                            mediaController.setAnchorView(videoView);
                            videoView.requestFocus();
                            videoView.start();
                        }

                        displayMoreInformation(operational_hours, website_link, how_to_get_there, best_time_to_visit, things_to_do, tags_key_word);

                        if (!longitude.isEmpty()||!latitude.isEmpty()){
                            destination = Point.fromLngLat(Double.parseDouble(longitude), Double.parseDouble(latitude));
                            fetchRoute();
                        }

                        Glide.with(this).load(LANDMARK_IMAGE_URL + landmark_img_1).into(binding.landmarkImage);
                        landmarkName.setText(name);
                        address.setText(landmarkAddress);
                        if (is_archive.equals("0")) {
//                            chipActiveStatus.setText("Active");
                            chipActiveStatus.setVisibility(View.VISIBLE);
                            chipInActiveStatus.setVisibility(View.GONE);
                        }else{
//                            chipInActiveStatus.setText("Temporary close");
                            chipInActiveStatus.setVisibility(View.VISIBLE);
                            chipActiveStatus.setVisibility(View.GONE);
                        }
                        Spanned spannedText = Html.fromHtml(description, Html.FROM_HTML_MODE_LEGACY);
                        landmarkDescription.setText(spannedText);

                        if (!landmark_img_2.isEmpty()) {
                            Glide.with(this).load(LANDMARK_IMAGE_URL + landmark_img_2).into(binding.landmarkImage2);
                            landmarkImageCard2.setVisibility(View.VISIBLE);
                        }
                        if (!landmark_img_3.isEmpty()) {
                            Glide.with(this).load(LANDMARK_IMAGE_URL + landmark_img_3).into(binding.landmarkImage3);
                            landmarkImageCard3.setVisibility(View.VISIBLE);
                        }
                        if (!landmark_img_4.isEmpty()) {
                            Glide.with(this).load(LANDMARK_IMAGE_URL + landmark_img_4).into(binding.landmarkImage4);
                            landmarkImageCard4.setVisibility(View.VISIBLE);
                        }
                        if (!landmark_img_5.isEmpty()) {
                            Glide.with(this).load(LANDMARK_IMAGE_URL + landmark_img_5).into(binding.landmarkImage5);
                            landmarkImageCard5.setVisibility(View.VISIBLE);
                        }

                        if (!description_2.isEmpty()) {
                        description2.setText(description_2);
                        description2.setVisibility(View.VISIBLE);
                        }

                        if (!description_3.isEmpty()) {
                            description3.setText(description_3);
                            description3.setVisibility(View.VISIBLE);
                        }

                        if (!description_4.isEmpty()) {
                            description4.setText(description_4);
                            description4.setVisibility(View.VISIBLE);
                        }

                        if (!description_5.isEmpty()) {
                            description5.setText(description_5);
                            description5.setVisibility(View.VISIBLE);
                        }

                        try {
                            if (speak_description.equals("true")) {
                                t1 = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
                                    @Override
                                    public void onInit(int status) {
                                        if (status == TextToSpeech.SUCCESS) {
                                            int result = t1.setLanguage(Locale.ENGLISH);
                                            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                                                Log.e("TTS", "Language not supported.");
                                            } else {
                                                // Speak the text
                                                Spanned landmark_description_html = Html.fromHtml(description, Html.FROM_HTML_MODE_LEGACY);
                                                t1.speak(landmark_description_html.toString(), TextToSpeech.QUEUE_FLUSH, null);
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

    private void displayMoreInformation(String str_operational_hours, String str_website_link, String str_how_to_get_there, String str_best_time_to_visit, String str_things_to_do, String str_tags_key_word ) {
        operational_hour.setText(str_operational_hours);
        website_link.setText(str_website_link);
        best_time_to_visit.setText(str_best_time_to_visit);
        tags_keyword.setText(str_tags_key_word);
        Spanned str_things_to_do_html = Html.fromHtml(str_things_to_do, Html.FROM_HTML_MODE_LEGACY);
        things_to_do.setText(str_things_to_do_html);
        Spanned str_how_to_get_there_html = Html.fromHtml(str_how_to_get_there, Html.FROM_HTML_MODE_LEGACY);
        how_to_get_there.setText(str_how_to_get_there_html);
    }

    public void checkPermissions() {
        if (ContextCompat.checkSelfPermission(LandmarkDetailActivity.this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(LandmarkDetailActivity.this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(LandmarkDetailActivity.this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            } else {
                ActivityCompat.requestPermissions(LandmarkDetailActivity.this,
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
                        try {
                            LandmarkDetailActivity.this.route = route;
                            navNextbillionMap.drawRoute(route);
                            button.setVisibility(View.VISIBLE);
                        } catch (Exception e) {
                            // Handle the exception here
                            e.printStackTrace(); // Or you can log the exception or take other appropriate actions.
                        }
                    }else
                        Snackbar.make(mapView, "Select longer route", Snackbar.LENGTH_LONG).show();
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

        private final WeakReference<LandmarkDetailActivity> activityWeakReference;

        NavigationLauncherLocationCallback(LandmarkDetailActivity activity) {
            this.activityWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void onSuccess(LocationEngineResult result) {
            LandmarkDetailActivity activity = activityWeakReference.get();
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
