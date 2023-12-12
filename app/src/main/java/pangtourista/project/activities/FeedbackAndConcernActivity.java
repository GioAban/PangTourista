package pangtourista.project.activities;

import static pangtourista.project.utils.Constants.USER_IMAGE_URL;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pangtourista.project.R;
import pangtourista.project.Sessions.SessionManager;
import pangtourista.project.utils.Constants;

public class FeedbackAndConcernActivity extends AppCompatActivity {
    SessionManager sessionManager;
    TextView username, user_email;
    TextInputEditText user_message;
    ImageButton btnSubmitConcern;
    private Toolbar toolbar;
    String set_username, set_email;

    private final int IMG_REQUEST = 1;
    private Bitmap bitmap;
    private boolean changeImage = false;
    private ImageView feedback_image, image_preview;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_feedback_and_concern);

        toolbar = findViewById(R.id.feedback_concern_bar_layout);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Feedback and Concern's");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        int whiteColor = ContextCompat.getColor(this, android.R.color.white);
        toolbar.setTitleTextColor(whiteColor);
        setNavigationIconColor(toolbar, whiteColor);

        sessionManager = new SessionManager(this);
        String user_id = sessionManager.getUserDetail().get("USER_ID");
        user_email = findViewById(R.id.user_email);
        username = findViewById(R.id.username);
        user_message = findViewById(R.id.user_message);
        btnSubmitConcern = findViewById(R.id.btnSubmitConcern);
        feedback_image = findViewById(R.id.feedback_image);
        image_preview = findViewById(R.id.image_preview);
        getProfileDetail(user_id);

        ActivityResultLauncher<Intent> activityResultLauncher =
                registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode()== Activity.RESULT_OK)
                        {
                            Intent data = result.getData();
                            Uri uri = data.getData();
                            try {
                                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                                image_preview.setImageBitmap(bitmap);
                                image_preview.setVisibility(View.VISIBLE);
                                changeImage = true;
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }
                    }
                });


        feedback_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                activityResultLauncher.launch(intent);
            }
        });




            btnSubmitConcern.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {submitMessage(user_id);}
        });


    }



    private void getProfileDetail(String user_id) {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = Constants.API_BASE_URL+"/users/get-profile-detail.php?user_id=" + user_id;
        StringRequest request = new StringRequest(Request.Method.GET, url, response -> {
            try {
                JSONObject object = new JSONObject(response);
                if (object.getString("status").equals("success")) {
                    JSONArray profileArray = object.getJSONArray("profiles");
                    if (profileArray.length() > 0) {
                        JSONObject profile = profileArray.getJSONObject(0);
                        String str_username = profile.getString("user_name");
                        String str_user_email = profile.getString("user_email");

                        username.setText(str_username);
                        user_email.setText(str_user_email);

                        set_username = str_username;
                        set_email = str_user_email;

                    } else {
                        // Handle the case when no landmarks were found
                    }
                } else if (object.getString("status").equals("empty")) {
                    // Handle the case when no landmarks were found
                } else {
                    // Handle the err case
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


    private void submitMessage(String user_id) {
        String base64Image = "";
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        if (bitmap != null) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            byte[] bytes = byteArrayOutputStream.toByteArray();
            base64Image = Base64.encodeToString(bytes, Base64.DEFAULT);
        }

        String str_user_message = user_message.getText().toString().trim();
        if(str_user_message.isEmpty())
        {
            Toast.makeText(this, "Please input your concern's!", Toast.LENGTH_SHORT).show();
        }
        else {
            if (containsBadWord(str_user_message)) {

            } else {
                //Api Start
//            progressBar.setVisibility(View.VISIBLE);

                RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
                String url = Constants.API_BASE_URL + "/users/submit-feedback-concern.php";

                String finalBase64Image = base64Image;
                StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                        response -> {
//                            progressBar.setVisibility(View.GONE);
                            if (response.equals("success")) {
                                Toast.makeText(FeedbackAndConcernActivity.this, "Thank you for submitting your concern!", Toast.LENGTH_SHORT).show();
                                user_message.setText("");
                            } else {
                                Toast.makeText(FeedbackAndConcernActivity.this, response, Toast.LENGTH_LONG).show();
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
//                    progressBar.setVisibility(View.GONE);
                        error.printStackTrace();
                    }
                }) {
                    protected Map<String, String> getParams() {
                        Map<String, String> paramV = new HashMap<>();
                        paramV.put("user_id", user_id);
                        paramV.put("user_message", str_user_message);
                        paramV.put("feedback_image", finalBase64Image);
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
        }
    }
    private boolean containsBadWord(String text) {
        List<String> badWords = Arrays.asList("putangena", "kapangit", "shet", "Ukininana",
                "Oketnana", "gago", "gagi", "tanga", "puta", "panget", "tangina", "buwisit", "inutil", "fuck", "bobo", "iyot","pangit", "ukininana", "okininana", "tarantado", "shit",
                "putang ina", "putangina", "tangina","bobo ka", "ulol", "gunggong", "hayop", "gaga", "leche", "uto-uto", "tangang", "stupid", "idiot", "moron",
                "asshole", "bastard", "dumbass", "fucker", "motherfucker", "shitty", "wanker", "jerk", "c*nt", "duckhead", "pr*ck"
        );

        // Split the input text into words
        String[] words = text.split("\\s+");

        // Check if any of the words are in the list of bad words
        for (String word : words) {
            if (badWords.contains(word.toLowerCase())) {
                Toast.makeText(this, "The comment contains a bad word: " + word, Toast.LENGTH_SHORT).show();
                return true;
            }
        }
        return false;
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