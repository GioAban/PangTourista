package pangtourista.project.activities;

import static pangtourista.project.utils.Constants.MUNICIPALITY_IMAGE_URL;
import static pangtourista.project.utils.Constants.USER_IMAGE_URL;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import pangtourista.project.Adapters.CommentAdapter;
import pangtourista.project.EmailLoginRegister.EmailRegisterActivity;
import pangtourista.project.Models.Comment;
import pangtourista.project.Models.Landmark;
import pangtourista.project.R;
import pangtourista.project.Sessions.SessionManager;
import pangtourista.project.databinding.ActivityMainBinding;
import pangtourista.project.databinding.ActivityProfileBinding;
import pangtourista.project.utils.Constants;

public class ProfileActivity extends AppCompatActivity {
    ActivityProfileBinding binding;
    SessionManager sessionManager;
    private CircleImageView imageUpload, selectImageFromGallery;

    TextInputEditText user_email, username, user_phone_number;
    String[] items = {"Male", "Female", "Other"};
    AutoCompleteTextView sex;
    ArrayAdapter<String> adapterItems;
    Button btnUpdateProfile;

    CommentAdapter commentAdapter;
    ArrayList<Comment> arrComments;

    private final int IMG_REQUEST = 1;
    private Bitmap bitmap;
    private boolean changeImage = false;

    private boolean isUpdateSex = false;
    String sex_setted_by_db;
    private Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(this);
        String user_id = sessionManager.getUserDetail().get("USER_ID");

        toolbar = findViewById(R.id.user_profile);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        int whiteColor = ContextCompat.getColor(this, android.R.color.white);
        toolbar.setTitleTextColor(whiteColor);
        setNavigationIconColor(toolbar, whiteColor);

        user_email = findViewById(R.id.user_email);
        username = findViewById(R.id.username);
        user_phone_number = findViewById(R.id.phone_number);
        sex = findViewById(R.id.sex);
        adapterItems = new ArrayAdapter<String>(this, R.layout.list_item, items);
        sex.setAdapter(adapterItems);
        sex.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String item = parent.getItemAtPosition(position).toString();
                isUpdateSex = true;
            }
        });

        imageUpload = findViewById(R.id.profile_image);
        btnUpdateProfile = findViewById(R.id.btnUpdateProfile);
        selectImageFromGallery = findViewById(R.id.selectImageFromGallery);

        arrComments = new ArrayList<>();
        commentAdapter = new CommentAdapter(this, arrComments);
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
                        imageUpload.setImageBitmap(bitmap);
                        changeImage = true;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }
        });

        selectImageFromGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                activityResultLauncher.launch(intent);
            }
        });

        btnUpdateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUserProfileInfo(user_id);
                if (changeImage==true){
                    updateUserProfileImage(user_id);
                }
            }
        });
    }

    private void updateUserProfileImage(String user_id) {

        ByteArrayOutputStream byteArrayOutputStream;
        byteArrayOutputStream = new ByteArrayOutputStream();
        if (bitmap != null)
        {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            byte[] bytes = byteArrayOutputStream.toByteArray();
            final String base64Image = Base64.encodeToString(bytes, Base64.DEFAULT);

            RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
            String url = Constants.API_BASE_URL+"/users/update-profile-image.php";

            StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                    response -> {
//                            progressBar.setVisibility(View.GONE);
                        if (response.equals("success"))
                        {
                            Toast.makeText(ProfileActivity.this, "Image upload successfully!", Toast.LENGTH_SHORT).show();

                        }
                        else
                        {
                            Toast.makeText(ProfileActivity.this, "Failed to upload image", Toast.LENGTH_LONG).show();
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
//                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(ProfileActivity.this, "Failed to upload image", Toast.LENGTH_LONG).show();
                }
            }){
                protected Map<String, String> getParams(){
                    Map<String, String> paramV = new HashMap<>();
                    paramV.put("image", base64Image);
                    paramV.put("user_id", user_id);
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
        else
        {
            Toast.makeText(ProfileActivity.this, "Select the image first", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateUserProfileInfo(String user_id) {
        String str_username = username.getText().toString();
        String phone_number = user_phone_number.getText().toString();
        String str_sex;
        if (!isUpdateSex){
            str_sex = sex_setted_by_db;
        }else{
            str_sex = sex.getText().toString();
        }


            RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
            String url = Constants.API_BASE_URL+"/users/update-profile-detail.php";

            StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                    response -> {
//                            progressBar.setVisibility(View.GONE);
                        if (response.equals("success"))
                        {
                            Toast.makeText(ProfileActivity.this, "Profile Update Successfully!", Toast.LENGTH_SHORT).show();

                        }
                        else
                        {
                            Toast.makeText(ProfileActivity.this, "Failed to upload image", Toast.LENGTH_LONG).show();
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
//                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(ProfileActivity.this, "Failed to upload image", Toast.LENGTH_LONG).show();
                }
            }){
                protected Map<String, String> getParams(){
                    Map<String, String> paramV = new HashMap<>();
                    paramV.put("user_name", str_username);
                    paramV.put("user_sex", str_sex);
                    paramV.put("phone_number", phone_number);
                    paramV.put("user_id", user_id);
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
                        String str_user_email = profile.getString("user_email");
                        String str_username = profile.getString("user_name");
                        String phone_number = profile.getString("phone_number");
                        String str_sex = profile.getString("user_sex");
                        String str_image = profile.getString("user_photo");
                        int id = profile.getInt("user_id");

                        user_email.setText(str_user_email);
                        username.setText(str_username);
                        user_phone_number.setText(phone_number);
                        sex.setHint(str_sex);
                        sex_setted_by_db = str_sex;
                        Picasso.get().load(USER_IMAGE_URL+str_image).into(imageUpload);

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

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
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