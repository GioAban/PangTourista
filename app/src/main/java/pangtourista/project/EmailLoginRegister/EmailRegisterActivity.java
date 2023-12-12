package pangtourista.project.EmailLoginRegister;

import static pangtourista.project.utils.Constants.API_BASE_URL;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.blogspot.atifsoftwares.animatoolib.Animatoo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import pangtourista.project.OperationRetrofitApi.ApiClient;
import pangtourista.project.OperationRetrofitApi.ApiInterface;
import pangtourista.project.OperationRetrofitApi.Users;
import pangtourista.project.R;
import pangtourista.project.Sessions.SessionManager;
import pangtourista.project.activities.MainActivity;
import pangtourista.project.activities.PrivacyActivity;
import pangtourista.project.activities.PrivacyPolicyActivity;
import pangtourista.project.activities.ProfileActivity;
import pangtourista.project.utils.Constants;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class EmailRegisterActivity extends AppCompatActivity {
    private EditText name, email, phone_number, password, confirm_password;
    String[] items = {"Male", "Female", "Other"};
    AutoCompleteTextView sex;
    ArrayAdapter<String> adapterItems;

    private final int IMG_REQUEST = 1;
    private Bitmap bitmap;
    private boolean changeImage = false;
    private Button regBtn, btnUploadImage;
    private ImageButton imageUpload;

    public static ApiInterface apiInterface;
    String user_id;
    SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_register);

        //----------------status bar hide----------------
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //----------------status bar hide end----------------
        apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        sessionManager = new SessionManager(this);
        init();

    }

    private void init(){
        name = findViewById(R.id.name);
        email = findViewById(R.id.email);
        phone_number = findViewById(R.id.phone_number);
        sex = findViewById(R.id.sex);
        imageUpload = findViewById(R.id.imageUpload);

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


        imageUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                activityResultLauncher.launch(intent);
            }
        });

        adapterItems = new ArrayAdapter<String>(this, R.layout.list_item, items);
        sex.setAdapter(adapterItems);
        sex.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String item = parent.getItemAtPosition(position).toString();
            }
        });

        password = findViewById(R.id.password);
        confirm_password = findViewById(R.id.confirm_password);
        regBtn = findViewById(R.id.button2);
        regBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              Registration();
            }
        });

    }

    private void Registration() {
        String user_name = name.getText().toString().trim();
        String user_email = email.getText().toString().trim();
        String user_phone_number = phone_number.getText().toString().trim();
        String user_sex = sex.getText().toString().trim();
        String user_password = password.getText().toString().trim();
        String user_confirm_password = confirm_password.getText().toString().trim();

        if (TextUtils.isEmpty(user_name)){
            Toast.makeText(this, "Please input your Username!", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(user_email)){
            Toast.makeText(this, "Please input your Email!", Toast.LENGTH_SHORT).show();
        }
        else if (!isValidEmail(user_email)) {
            Toast.makeText(this, "Invalid Email format!", Toast.LENGTH_SHORT).show();
        }
        else if (!TextUtils.isEmpty(user_phone_number) && !user_phone_number.matches("\\d{11}")) {
            Toast.makeText(this, "Invalid phone number!", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(user_sex)){
            Toast.makeText(this, "Please input your Sex!", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(user_password)){
            Toast.makeText(this, "Please input your password!", Toast.LENGTH_SHORT).show();
        }
        else if (user_password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters long!", Toast.LENGTH_SHORT).show();
        }
        else if (!containsNumber(user_password)) {
            Toast.makeText(this, "Password must contain at least one number!", Toast.LENGTH_SHORT).show();
        }
        else if (!user_password.equals(user_confirm_password)){
            Toast.makeText(this, "Your Password and Confirm Password do not match!", Toast.LENGTH_SHORT).show();
        }
        else
        {

            ProgressDialog dialog = new ProgressDialog(this);
            dialog.setTitle("Registering...");
            dialog.setMessage("Please wait while we are adding your credentials");
            dialog.show();
            dialog.setCanceledOnTouchOutside(false);

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            if (bitmap != null) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                byte[] bytes = byteArrayOutputStream.toByteArray();
                final String base64Image = Base64.encodeToString(bytes, Base64.DEFAULT);

                RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
                String url = Constants.API_BASE_URL + "/users/email_registration.php";

                StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                        response -> {
//                            progressBar.setVisibility(View.GONE);

                            if (response.equals("ok"))
                            {
                                Toast.makeText(this, "Account is created. Please login to verify!", Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(getApplicationContext(), EmailLoginActivity.class);
                                startActivity(intent);
                                finish();
                                Animatoo.animateZoom(EmailRegisterActivity.this);
                                dialog.dismiss();
                            }
                            else
                            {
                                Toast.makeText(this, response, Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
//                    progressBar.setVisibility(View.GONE);
                        Toast.makeText(EmailRegisterActivity.this, "Failed to upload image", Toast.LENGTH_LONG).show();
                    }
                }){
                    protected Map<String, String> getParams(){
                        Map<String, String> paramV = new HashMap<>();
                        paramV.put("user_photo", base64Image);
                        paramV.put("user_name", user_name);
                        paramV.put("user_email", user_email);
                        paramV.put("phone_number", user_phone_number);
                        paramV.put("user_sex", 	user_sex);
                        paramV.put("user_password", user_password);
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
                Toast.makeText(EmailRegisterActivity.this, "Please upload an image!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }

        }
    }

    // Method to check if the password contains at least one number
    private boolean containsNumber(String password) {
        // Use regular expression to check for at least one digit
        String numberPattern = ".*\\d.*";
        return password.matches(numberPattern);
    }

    private boolean isValidEmail(String email) {
        String emailPattern = "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}";
        Pattern pattern = Pattern.compile(emailPattern);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    public void goToLogin(View view) {
        Intent intent = new Intent(EmailRegisterActivity.this, EmailLoginActivity.class);
        startActivity(intent);
        Animatoo.animateSwipeLeft(this);
        finish();
    }

    public void backToMainPage(View view) {
        Intent intent = new Intent(EmailRegisterActivity.this, MainActivity.class);
        startActivity(intent);
        Animatoo.animateSwipeRight(this);
        finish();
    }

    public void goToTermsAndCondition(View view) {
        Intent intent = new Intent(EmailRegisterActivity.this, PrivacyActivity.class);
        startActivity(intent);
        Animatoo.animateSwipeRight(this);
    }

    public void gotoDataPrivacy(View view) {
        Intent intent = new Intent(EmailRegisterActivity.this, PrivacyPolicyActivity.class);
        startActivity(intent);
        Animatoo.animateSwipeRight(this);
    }


//    public void clickImage(View view) {
//        ByteArrayOutputStream byteArrayOutputStream;
//        byteArrayOutputStream = new ByteArrayOutputStream();
//
//        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
//        byte[] bytes = byteArrayOutputStream.toByteArray();
//        final String base64Image = Base64.encodeToString(bytes, Base64.DEFAULT);
//        Toast.makeText(this, base64Image, Toast.LENGTH_SHORT).show();
//    }
}