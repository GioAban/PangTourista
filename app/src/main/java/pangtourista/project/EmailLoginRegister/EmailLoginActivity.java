package pangtourista.project.EmailLoginRegister;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieDrawable;
import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.google.android.material.textfield.TextInputEditText;

import pangtourista.project.OperationRetrofitApi.ApiClient;
import pangtourista.project.OperationRetrofitApi.ApiInterface;
import pangtourista.project.OperationRetrofitApi.Users;
import pangtourista.project.R;
import pangtourista.project.Sessions.SessionManager;
import pangtourista.project.activities.MainActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EmailLoginActivity extends AppCompatActivity {

    LottieAnimationView lottie;
    private TextInputEditText email, password;
    private TextView forgotPassword;
    private Button btnLogin;
    public static ApiInterface apiInterface;
    String user_id;
    SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_login);

        //----------------status bar hide----------------
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //----------------status bar hide end----------------
        apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        sessionManager = new SessionManager(this);


        init();
    }
    private void init(){
            lottie = findViewById(R.id.lottie);
            lottie.setRepeatCount(LottieDrawable.INFINITE);
            lottie.playAnimation();

            email = findViewById(R.id.email);
            password = findViewById(R.id.password);
            btnLogin = findViewById(R.id.button2);
            forgotPassword = findViewById(R.id.forgotPassword);

            btnLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Login();
                }
            });

            forgotPassword.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SendUserToForgotPasswordActivity();
                }
            });

    }

    private void Login() {
        String user_email_or_phone_number = email.getText().toString().trim();
        String user_password = password.getText().toString().trim();
        if (TextUtils.isEmpty(user_email_or_phone_number)){
            Toast.makeText(this, "Please input your email!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!user_email_or_phone_number.matches("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$")
                && !user_email_or_phone_number.matches("\\d{11}")) {
            Toast.makeText(this, "Please enter a valid email or phone number!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(user_password)){
            Toast.makeText(this, "Please input your password!", Toast.LENGTH_SHORT).show();
            return;
        }
            ProgressDialog dialog = new ProgressDialog(this);
            dialog.setTitle("Logging...");
            dialog.setMessage("Please wait we are checking your credentials");
            dialog.show();
            dialog.setCanceledOnTouchOutside(false);
            Call<Users> call = apiInterface.performEmailLogin(user_email_or_phone_number, user_password);
            call.enqueue(new Callback<Users>() {
                @Override
                public void onResponse(Call<Users> call, Response<Users> response) {
                    if(response.body().getResponse().equals("ok"))
                    {
                        user_id = response.body().getUserId();
                        sessionManager.createSession(user_id);
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intent);
                        finish();
                        Animatoo.animateSplit(EmailLoginActivity.this);
                        dialog.dismiss();
                    }
                    else if(response.body().getResponse().equals("no_account"))
                    {
                        Toast.makeText(EmailLoginActivity.this, "Email or Password is incorrect!", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                    else if(response.body().getResponse().equals("not_verified"))
                    {
                        Toast.makeText(EmailLoginActivity.this, "Your account is not verified", Toast.LENGTH_LONG).show();
                        dialog.dismiss();
                        goToEmailOrAccountVerificationOTP(user_email_or_phone_number);
                    }
                }
                @Override
                public void onFailure(Call<Users> call, Throwable t) {
                    Toast.makeText(EmailLoginActivity.this, "Your connection is weak!", Toast.LENGTH_SHORT).show();
                }
            });
    }


    private void goToEmailOrAccountVerificationOTP(String user_email_or_phone_number) {
        Intent intent = new Intent(getApplicationContext(), EmailVerificationOTP.class);
        intent.putExtra("user_email_or_phone_number", user_email_or_phone_number);
        startActivity(intent);
        Animatoo.animateFade(EmailLoginActivity.this);
        finish();
    }

    public void goToRegister(View view) {
        Intent intent = new Intent(EmailLoginActivity.this, EmailRegisterActivity.class);
        startActivity(intent);
        Animatoo.animateFade(this);
    }

    public void backToMainPage(View view) {
        Intent intent = new Intent(EmailLoginActivity.this, MainActivity.class);
        startActivity(intent);
        Animatoo.animateFade(EmailLoginActivity.this);
        finish();
    }

    private void SendUserToForgotPasswordActivity() {
        Intent forgotPasswordIntent = new Intent(getApplicationContext(), ForgotPasswordActivity.class);
        startActivity(forgotPasswordIntent);
        Animatoo.animateFade(EmailLoginActivity.this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (sessionManager.isLogin())
        {
            Intent intent = new Intent(EmailLoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
            Animatoo.animateSplit(this);
        }
        else
        {

        }
    }

}