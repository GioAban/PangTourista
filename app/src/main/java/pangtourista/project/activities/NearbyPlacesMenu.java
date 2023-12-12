package pangtourista.project.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieDrawable;

import pangtourista.project.R;

public class NearbyPlacesMenu extends AppCompatActivity {
    LottieAnimationView lottie;
    CardView cardHospital,
            cardGasStation,
            cardBank,
            cardPoliceStation,
            cardRestaurant,
            cardParking,
            cardHotel,
            cardSupermarket,
            cardRepair,
            cardBusStation,
            cardAtm;

    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_nearby_places_menu);

        lottie = findViewById(R.id.lottie_nearby_menu);
        lottie.setRepeatCount(LottieDrawable.INFINITE);
        lottie.playAnimation();

        toolbar = findViewById(R.id.nearby_place);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Find Nearby Places");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        int whiteColor = ContextCompat.getColor(this, android.R.color.white);
        toolbar.setTitleTextColor(whiteColor);
        setNavigationIconColor(toolbar, whiteColor);

                cardHospital = findViewById(R.id.card_hospital);
                cardGasStation = findViewById(R.id.card_gas_station);
                cardBank = findViewById(R.id.card_bank);
                cardPoliceStation = findViewById(R.id.card_police_station);
                cardRestaurant = findViewById(R.id.card_restaurant);
                cardParking = findViewById(R.id.card_parking);
                cardHotel = findViewById(R.id.card_hotel);
                cardSupermarket = findViewById(R.id.card_supermarket);
                cardRepair = findViewById(R.id.card_repair);
                cardBusStation = findViewById(R.id.card_bus_station);
                cardAtm = findViewById(R.id.card_atm);

        cardHospital.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), GetNearbyPlaces.class);
                intent.putExtra("place_type", "hospital");
                startActivity(intent);
            }
        });

        cardGasStation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), GetNearbyPlaces.class);
                intent.putExtra("place_type", "gas_station");
                startActivity(intent);
            }
        });

        cardBank.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), GetNearbyPlaces.class);
                intent.putExtra("place_type", "bank");
                startActivity(intent);
            }
        });

        cardPoliceStation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), GetNearbyPlaces.class);
                intent.putExtra("place_type", "police");
                startActivity(intent);
            }
        });

        cardRestaurant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), GetNearbyPlaces.class);
                intent.putExtra("place_type", "restaurant");
                startActivity(intent);
            }
        });

        cardParking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), GetNearbyPlaces.class);
                intent.putExtra("place_type", "park");
                startActivity(intent);
            }
        });

        cardHotel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), GetNearbyPlaces.class);
                intent.putExtra("place_type", "hotel");
                startActivity(intent);
            }
        });

        cardSupermarket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), GetNearbyPlaces.class);
                intent.putExtra("place_type", "supermarket");
                startActivity(intent);
            }
        });

        cardRepair.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), GetNearbyPlaces.class);
                intent.putExtra("place_type", "car_repair");
                startActivity(intent);
            }
        });


        cardBusStation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), GetNearbyPlaces.class);
                intent.putExtra("place_type", "bus_station");
                startActivity(intent);
            }
        });

        cardAtm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), GetNearbyPlaces.class);
                intent.putExtra("place_type", "atm");
                startActivity(intent);
            }
        });
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

    private void setNavigationIconColor(androidx.appcompat.widget.Toolbar toolbar, int color) {
        if (toolbar.getNavigationIcon() != null) {
            toolbar.getNavigationIcon().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        }
    }
}