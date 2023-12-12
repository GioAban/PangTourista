package pangtourista.project.activities;

import static pangtourista.project.utils.Constants.MUNICIPALITY_IMAGE_URL;
import static pangtourista.project.utils.Constants.NEWS_EVENTS_IMAGE_URL;
import static pangtourista.project.utils.Constants.USER_IMAGE_URL;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;


import android.graphics.PorterDuff;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;

import android.text.Html;
import android.text.Spanned;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.squareup.picasso.Picasso;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.Date;

import pangtourista.project.R;
import pangtourista.project.Sessions.SessionManager;
import pangtourista.project.databinding.ActivityNewsEventsDetailBinding;
import pangtourista.project.utils.Constants;

public class NewsEventsDetailActivity extends AppCompatActivity {

    ActivityNewsEventsDetailBinding binding;
    TextView neDescription, neTitle, ne_created_at, ne_image_source;
    SessionManager sessionManager;
    String loggedInUserId;
    AlertDialog dialog;
    Button more_image;
    ImageView image1,image2, image3, image4, image5, image6, image7, image8, image9, image10;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        binding = ActivityNewsEventsDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        sessionManager = new SessionManager(this);
        loggedInUserId = sessionManager.getUserDetail().get("USER_ID");

        toolbar = findViewById(R.id.view_news);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("News/Event Details");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        int whiteColor = ContextCompat.getColor(this, android.R.color.white);
        toolbar.setTitleTextColor(whiteColor);
        setNavigationIconColor(toolbar, whiteColor);

        neTitle = findViewById(R.id.neTitle);
        neDescription = findViewById(R.id.neDescription);
        ne_created_at = findViewById(R.id.ne_created_at);
        ne_image_source = findViewById(R.id.ne_image_source);
        more_image = findViewById(R.id.more_image);

        int ne_id = getIntent().getIntExtra("ne_id",0);
        String ne_image = getIntent().getStringExtra("ne_image");

        //Calling the function for getting informations
        getNewsEventsDetails(ne_id);


        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("More image");
        View view = getLayoutInflater().inflate(R.layout.view_ne_image_custom_dialog, null);
        //View dialog
        builder.setView(view);
        //Create dialog
        dialog = builder.create();
        image1 = view.findViewById(R.id.image1);
        image2 = view.findViewById(R.id.image2);
        image3 = view.findViewById(R.id.image3);
        image4 = view.findViewById(R.id.image4);
        image5 = view.findViewById(R.id.image5);
        image6 = view.findViewById(R.id.image6);
        image7 = view.findViewById(R.id.image7);
        image8 = view.findViewById(R.id.image8);
        image9 = view.findViewById(R.id.image9);
        image10 = view.findViewById(R.id.image10);

        dialog.dismiss();
        more_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();
            }
        });



    }



    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
    void getNewsEventsDetails(int id) {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = Constants.API_BASE_URL+"/users/get-news-events-detail.php?id=" + id;
        StringRequest request = new StringRequest(Request.Method.GET, url, response -> {
            try {
                JSONObject object = new JSONObject(response);
                if (object.getString("status").equals("success")) {
                    JSONArray newsEventsArray = object.getJSONArray("news_events");
                    if (newsEventsArray.length() > 0) {
                        JSONObject news_events = newsEventsArray.getJSONObject(0);
                        String ne_image = news_events.getString("ne_image");
                        String name = news_events.getString("ne_title");
                        String image_source = news_events.getString("image_source");
                        String description = news_events.getString("ne_description");
                        String str_ne_created_at = news_events.getString("ne_created_at");

                        String[] imageArray = ne_image.split(",");
                        String str_image1 = "";

                        if (imageArray.length > 0) {
                            String str_modal_image1, str_modal_image2, str_modal_image3, str_modal_image4, str_modal_image5 = "";
                            String str_modal_image6, str_modal_image7, str_modal_image8, str_modal_image9 = "";
                            str_image1 = imageArray[0].trim(); // Get the first part and trim any leading/trailing spaces


                            Glide.with(this).load(NEWS_EVENTS_IMAGE_URL + str_image1).into(binding.neImage);

                            str_modal_image1 = (imageArray.length > 1) ? imageArray[1].trim() : "";
                            str_modal_image2 = (imageArray.length > 2) ? imageArray[2].trim() : "";
                            str_modal_image3 = (imageArray.length > 3) ? imageArray[3].trim() : "";
                            str_modal_image4 = (imageArray.length > 4) ? imageArray[4].trim() : "";
                            str_modal_image5 = (imageArray.length > 5) ? imageArray[5].trim() : "";
                            str_modal_image6 = (imageArray.length > 6) ? imageArray[6].trim() : "";
                            str_modal_image7 = (imageArray.length > 7) ? imageArray[7].trim() : "";
                            str_modal_image8 = (imageArray.length > 8) ? imageArray[8].trim() : "";
                            str_modal_image9 = (imageArray.length > 9) ? imageArray[9].trim() : "";


                            if (!str_modal_image1.equals("")) {
                                Glide.with(this).load(NEWS_EVENTS_IMAGE_URL + str_modal_image1).into(image1);
                                image1.setVisibility(View.VISIBLE);
                            }
                            if (!str_modal_image2.equals("")) {
                                Glide.with(this).load(NEWS_EVENTS_IMAGE_URL + str_modal_image2).into(image2);
                                image2.setVisibility(View.VISIBLE);
                            }
                            if (!str_modal_image3.equals("")) {
                                Glide.with(this).load(NEWS_EVENTS_IMAGE_URL + str_modal_image3).into(image3);
                                image3.setVisibility(View.VISIBLE);
                            }
                            if (!str_modal_image4.equals("")) {
                                Glide.with(this).load(NEWS_EVENTS_IMAGE_URL + str_modal_image4).into(image4);
                                image4.setVisibility(View.VISIBLE);
                            }
                            if (!str_modal_image5.equals("")) {
                                Glide.with(this).load(NEWS_EVENTS_IMAGE_URL + str_modal_image5).into(image5);
                                image5.setVisibility(View.VISIBLE);
                            }
                            if (!str_modal_image6.equals("")) {
                                Glide.with(this).load(NEWS_EVENTS_IMAGE_URL + str_modal_image6).into(image6);
                                image6.setVisibility(View.VISIBLE);
                            }
                            if (!str_modal_image7.equals("")) {
                                Glide.with(this).load(NEWS_EVENTS_IMAGE_URL + str_modal_image7).into(image7);
                                image7.setVisibility(View.VISIBLE);
                            }
                            if (!str_modal_image8.equals("")) {
                                Glide.with(this).load(NEWS_EVENTS_IMAGE_URL + str_modal_image8).into(image8);
                                image8.setVisibility(View.VISIBLE);
                            }
                            if (!str_modal_image9.equals("")) {
                                Glide.with(this).load(NEWS_EVENTS_IMAGE_URL + str_modal_image9).into(image9);
                                image9.setVisibility(View.VISIBLE);
                            }

                        } else {
                            System.out.println("No images found in the string.");
                        }



                        // Assuming that str_ne_created_at is in a specific date format, e.g., "yyyy-MM-dd HH:mm:ss"
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        Date date = sdf.parse(str_ne_created_at);

                        // Format the date to a desired text format, e.g., "MMMM dd, yyyy hh:mm a"
                        SimpleDateFormat outputFormat = new SimpleDateFormat("MMMM dd, yyyy hh:mm a");
                        String formattedDate = outputFormat.format(date);

                        neTitle.setText(name);
                        Spanned description_html = Html.fromHtml(description, Html.FROM_HTML_MODE_LEGACY);
                        neDescription.setText(description_html);

                        ne_created_at.setText(formattedDate);
                        ne_image_source.setText(image_source);
                        if (!image_source.equals("")){
                            ne_image_source.setVisibility(View.VISIBLE);
                        }

                    } else {
                        // Handle the case when no landmarks were found
                    }

                } else if (object.getString("status").equals("empty")) {
                    // Handle the case when no landmarks were found
                } else {
                    // Handle the error case
                }
            } catch (JSONException | ParseException e) {
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