package pangtourista.project.EmailLoginRegister;

import static pangtourista.project.utils.Constants.USER_IMAGE_URL;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.chaos.view.PinView;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pangtourista.project.R;
import pangtourista.project.utils.Constants;

public class EmailVerificationOTP extends AppCompatActivity {

    PinView pinView;
    Button submitOtp, resendCodeToEmail, resendCodeToPhoneNumber;
    TextView textEmailOrPhoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_verification_otp);

        // hookers (binding view)
        textEmailOrPhoneNumber = findViewById(R.id.textEmailOrPhoneNumber);
        pinView = findViewById(R.id.pinview);
        submitOtp = findViewById(R.id.submitOtp);
        resendCodeToEmail = findViewById(R.id.resendCodeToEmail);
        resendCodeToPhoneNumber = findViewById(R.id.resendCodeToPhoneNumber);


        //Get the email from login activity
        String user_email_or_phone_number = getIntent().getExtras().getString("user_email_or_phone_number");
        textEmailOrPhoneNumber.setText(user_email_or_phone_number);

        if (isEmailOrGmail(user_email_or_phone_number.trim())) {
            CheckEmail(user_email_or_phone_number);
        } else if (isPhoneNumber(user_email_or_phone_number.trim())) {
            CheckPhoneNumber(user_email_or_phone_number);
        } else {
            System.out.println("Invalid input.");
        }



        // setting onClickListener on Button
        submitOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // getting the PinView data
                String otp=pinView.getText().toString();
                if (otp.isEmpty()){
                    Toast.makeText(EmailVerificationOTP.this, "Please input the code!", Toast.LENGTH_SHORT).show();
                    return;
                }
                else if (otp.length()<=3 && !otp.isEmpty())
                {
                    Toast.makeText(EmailVerificationOTP.this, "Please input a 4 code number!", Toast.LENGTH_SHORT).show();
                    return;
                }
                else
                {
                    ProgressDialog dialog = new ProgressDialog(EmailVerificationOTP.this);
                    dialog.setTitle("Loading...");
                    dialog.setMessage("Verifying account please wait!");
                    dialog.show();
                    dialog.setCanceledOnTouchOutside(false);

                    RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
                    String url = Constants.API_BASE_URL+"/users/email-verification.php";

                    StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                            response -> {
//                            progressBar.setVisibility(View.GONE);
                                if (response.equals("success"))
                                {
                                    dialog.dismiss();
                                    Toast.makeText(EmailVerificationOTP.this, "Hooray! Your Email Successfully Verified", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(getApplicationContext(), EmailLoginActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                                else {
                                    Toast.makeText(EmailVerificationOTP.this, response, Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                }
                            }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
//                        progressBar.setVisibility(View.GONE);
                            error.printStackTrace();
                        }
                    }){
                        protected Map<String, String> getParams(){
                            Map<String, String> paramV = new HashMap<>();
                            paramV.put("user_email_or_phone_number", user_email_or_phone_number);
                            paramV.put("otp", otp);
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
        });

        resendCodeToEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendOtpToEmail(user_email_or_phone_number);
            }
        });

    }

    private void CheckPhoneNumber(String phone_number) {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = Constants.API_BASE_URL+"/users/check_phone_number.php?phone_number=" + phone_number;
        StringRequest request = new StringRequest(Request.Method.GET, url, response -> {
            try {
                JSONObject object = new JSONObject(response);
                if (object.getString("status").equals("success")) {
                    JSONArray usersArray = object.getJSONArray("phone_numbers");
                    if (usersArray.length() > 0) {
                        JSONObject user_details = usersArray.getJSONObject(0);
                        String str_phone_number = user_details.getString("phone_number");
                        String str_user_email = user_details.getString("user_email");

                        resendCodeToPhoneNumber.setVisibility(View.VISIBLE);
                        resendCodeToPhoneNumber.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                sendOtpToPhoneNumber(str_phone_number);
                            }
                        });

                        if (!str_user_email.isEmpty()){
                            resendCodeToEmail.setVisibility(View.VISIBLE);
                            resendCodeToEmail.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    sendOtpToEmail(str_user_email);
                                }
                            });
                        }

                    } else {
                        // No mobile phone found
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


    private void CheckEmail(String user_email) {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = Constants.API_BASE_URL+"/users/check_user_email.php?user_email=" + user_email;
        StringRequest request = new StringRequest(Request.Method.GET, url, response -> {
            try {
                JSONObject object = new JSONObject(response);
                if (object.getString("status").equals("success")) {
                    JSONArray landmarksArray = object.getJSONArray("user_emails");
                    if (landmarksArray.length() > 0) {
                        JSONObject user_details = landmarksArray.getJSONObject(0);
                        String str_user_email = user_details.getString("user_email");
                        String str_phone_number = user_details.getString("phone_number");

                        resendCodeToEmail.setVisibility(View.VISIBLE);
                        resendCodeToEmail.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                sendOtpToEmail(str_user_email);
                            }
                        });

                        if (!str_phone_number.isEmpty()){
                            resendCodeToPhoneNumber.setVisibility(View.VISIBLE);
                            resendCodeToPhoneNumber.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    sendOtpToPhoneNumber(str_phone_number);
                                }
                            });
                        }

                    } else {
                        // No mobile phone found
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

    private void sendOtpToEmail(String user_email) {
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setTitle("Please Wait...");
        dialog.setMessage("Sending verification code to your email address...");
        dialog.show();
        dialog.setCanceledOnTouchOutside(false);

        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String url = Constants.API_BASE_URL+"/users/send-email-verification.php";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    if (response.equals("success")) {
                        Toast.makeText(this, "We send a verification code to your email address for you to verify your account.", Toast.LENGTH_LONG).show();
//                        goToEmailVerificationOTP(email);
                        submitOtp.setVisibility(View.VISIBLE);
                        pinView.setVisibility(View.VISIBLE);
                        dialog.dismiss();
                    }else {
                        Toast.makeText(EmailVerificationOTP.this, "Something Went Wrong, Try Again!", Toast.LENGTH_SHORT).show();
                    }
                }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
//                progressBar.setVisibility(View.GONE);
                error.printStackTrace();
            }
        }) {
            protected Map<String, String> getParams() {
                Map<String, String> paramV = new HashMap<>();
                paramV.put("email", user_email);
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

    private void sendOtpToPhoneNumber(String str_phone_number) {
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setTitle("Please Wait...");
        dialog.setMessage("Sending verification code to your phone number...");
        dialog.show();
        dialog.setCanceledOnTouchOutside(false);

        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String url = Constants.API_BASE_URL+"/users/send-phone-number-verification.php";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    if (response.trim().equals("success")) {
                        Toast.makeText(this, "We send a verification code to your phone number for you to verify your account.", Toast.LENGTH_LONG).show();
                        submitOtp.setVisibility(View.VISIBLE);
                        pinView.setVisibility(View.VISIBLE);
                        dialog.dismiss();
                    }else {
                        Toast.makeText(EmailVerificationOTP.this, response.trim(), Toast.LENGTH_SHORT).show();
                    }
                }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
//                progressBar.setVisibility(View.GONE);
                error.printStackTrace();
            }
        }) {
            protected Map<String, String> getParams() {
                Map<String, String> paramV = new HashMap<>();
                paramV.put("phone_number", str_phone_number);
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


    private static boolean isEmailOrGmail(String input) {
        String emailOrGmailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        Pattern pattern = Pattern.compile(emailOrGmailRegex);
        Matcher matcher = pattern.matcher(input);
        return matcher.matches();
    }

    private static boolean isPhoneNumber(String input) {
        String phoneNumberRegex = "^\\d{11}$";
        Pattern pattern = Pattern.compile(phoneNumberRegex);
        Matcher matcher = pattern.matcher(input);
        return matcher.matches();
    }

}