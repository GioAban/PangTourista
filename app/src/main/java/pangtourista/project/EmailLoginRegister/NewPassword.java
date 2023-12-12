package pangtourista.project.EmailLoginRegister;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieDrawable;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;

import java.util.HashMap;
import java.util.Map;

import pangtourista.project.R;
import pangtourista.project.utils.Constants;

public class NewPassword extends AppCompatActivity {
    LottieAnimationView lottie;
    private TextInputEditText editTextNewPassword, editTextOTP;
    private Button resetPassword;
    private ProgressBar progressBar;
    String email;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_password);

        email = getIntent().getExtras().getString("email");

        lottie = findViewById(R.id.lottie);
        lottie.setRepeatCount(LottieDrawable.INFINITE);
        lottie.playAnimation();
        editTextNewPassword = findViewById(R.id.new_password);
        editTextOTP = findViewById(R.id.otp);
        resetPassword = findViewById(R.id.resetPassword);
//        progressBar = findViewById(R.id.progress);

        resetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ResetPassword();
            }
        });
    }

    private void ResetPassword() {

        String otp = editTextOTP.getText().toString().trim();
        String new_password = editTextNewPassword.getText().toString().trim();
        if(otp.isEmpty())
        {
            Toast.makeText(this, "Please input the otp!", Toast.LENGTH_SHORT).show();
        }
        else if (!otp.matches("\\d+"))
        {
            Toast.makeText(this, "Please input number only!", Toast.LENGTH_SHORT).show();
        }
        else if (!otp.matches("\\d{4}")) {
            Toast.makeText(this, "Please input a valid 4-digit number!", Toast.LENGTH_SHORT).show();
        }
        else if (new_password.isEmpty())
        {
            Toast.makeText(this, "Please input the new password!", Toast.LENGTH_SHORT).show();
        }
        else if (new_password.length() < 6)
        {
            Toast.makeText(this, "Password must be at least 6 characters!", Toast.LENGTH_SHORT).show();
        }
        else if (!new_password.matches(".*\\d.*")) {
            Toast.makeText(this, "Password must contain at least one numeric digit!", Toast.LENGTH_SHORT).show();
        }
        else
        {
            //Api Start
//            progressBar.setVisibility(View.VISIBLE);

            RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
            String url = Constants.API_BASE_URL+"/users/new-password.php";

            StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                    response -> {
//                            progressBar.setVisibility(View.GONE);
                        if (response.equals("success"))
                        {
                            Toast.makeText(NewPassword.this, "Password changed successfully!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(getApplicationContext(), EmailLoginActivity.class);
                            startActivity(intent);
                            finish();
                        }
                        else {
                            Toast.makeText(NewPassword.this, response, Toast.LENGTH_SHORT).show();
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    progressBar.setVisibility(View.GONE);
                    error.printStackTrace();
                }
            }){
                protected Map<String, String> getParams(){
                    Map<String, String> paramV = new HashMap<>();
                    paramV.put("email", email);
                    paramV.put("otp", otp);
                    paramV.put("new-password", new_password);
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