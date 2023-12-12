package pangtourista.project.EmailLoginRegister;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
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
import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.google.android.material.textfield.TextInputEditText;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pangtourista.project.R;
import pangtourista.project.utils.Constants;


public class ForgotPasswordActivity extends AppCompatActivity {

    LottieAnimationView lottie;
    TextInputEditText email;
    private ProgressBar progressBar;

    String user_id;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        lottie = findViewById(R.id.lottie);
        lottie.setRepeatCount(LottieDrawable.INFINITE);
        lottie.playAnimation();

        email = findViewById(R.id.email);
        progressBar = findViewById(R.id.progress);
        Button button = findViewById(R.id.btnSubmit);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String strEmail = email.getText().toString().trim();

                if (TextUtils.isEmpty(strEmail)){
                    Toast.makeText(getApplicationContext(), "Please input your email account!", Toast.LENGTH_SHORT).show();
                }
                else if (!isValidEmail(strEmail))
                {
                    Toast.makeText(getApplicationContext(), "Invalid Email format!", Toast.LENGTH_SHORT).show();
                }
                else {
                    progressBar.setVisibility(View.VISIBLE);
                    RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
                    String url = Constants.API_BASE_URL+"/users/forgot_password.php";
                    StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                            response -> {
                                progressBar.setVisibility(View.GONE);
                                if (response.equals("success")) {
                                    Intent intent = new Intent(getApplicationContext(), NewPassword.class);
                                    intent.putExtra("email", strEmail);
                                    startActivity(intent);
                                    Animatoo.animateZoom(ForgotPasswordActivity.this);
                                    finish();
                                    Toast.makeText(ForgotPasswordActivity.this, "We send a Forgot Password OTP code in your email address", Toast.LENGTH_SHORT).show();
                                }else{
                                    Toast.makeText(ForgotPasswordActivity.this, response, Toast.LENGTH_SHORT).show();
                                }


                            }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            progressBar.setVisibility(View.GONE);
                            error.printStackTrace();
                        }
                    }) {
                        protected Map<String, String> getParams() {
                            Map<String, String> paramV = new HashMap<>();
                            paramV.put("email", strEmail);
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
            }
        });
    }

    private boolean isValidEmail(String email) {
        String emailPattern = "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}";
        Pattern pattern = Pattern.compile(emailPattern);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }
}

