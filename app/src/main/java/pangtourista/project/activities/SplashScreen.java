package pangtourista.project.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;

import pangtourista.project.EmailLoginRegister.EmailLoginActivity;
import pangtourista.project.R;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        //----------------status bar hide----------------
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //----------------status bar hide end----------------

        //----------------Handler------------------
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                Intent intent = new Intent(SplashScreen.this, EmailLoginActivity.class);
                startActivity(intent);
                Animatoo.animateSlideDown(SplashScreen.this);
                finish();
            }
        }, 3000);
        //----------------End Handler--------------
    }
}