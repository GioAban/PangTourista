package pangtourista.project.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieDrawable;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.dialogflow.v2.DetectIntentResponse;
import com.google.cloud.dialogflow.v2.QueryInput;
import com.google.cloud.dialogflow.v2.SessionName;
import com.google.cloud.dialogflow.v2.SessionsClient;
import com.google.cloud.dialogflow.v2.SessionsSettings;
import com.google.cloud.dialogflow.v2.TextInput;
import com.google.common.collect.Lists;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

import pangtourista.project.Adapters.ChatAdapter;
import pangtourista.project.Models.Message;
import pangtourista.project.Models.Municipality;
import pangtourista.project.R;
import pangtourista.project.helpers.SendMessageInBg;
import pangtourista.project.interfaces.BotReply;
import pangtourista.project.utils.Constants;

public class VistualAssistantActivity extends AppCompatActivity implements BotReply {
    private final int REQ_CODE = 100;
    TextView ed1;
    LottieAnimationView lottie;

    //Virtual Assistant Section
    RecyclerView chatView;
    ChatAdapter chatAdapter;
    List<Message> messageList = new ArrayList<>();
    EditText editMessage;
    ImageButton btnSend;

    //dialogFlow
    private SessionsClient sessionsClient;
    private SessionName sessionName;
    private String uuid = UUID.randomUUID().toString();
    private String TAG = "mainactivity";
    TextToSpeech t1;
    String stringVoiceSpeechValue = "";
    AlertDialog dialog, va_guide_modal;
    TextView mainProduct;
    Button close_va;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_vistual_assistant);

        toolbar = findViewById(R.id.virtual_assistant);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Virtual Assistant");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        int whiteColor = ContextCompat.getColor(this, android.R.color.white);
        toolbar.setTitleTextColor(whiteColor);
        setNavigationIconColor(toolbar, whiteColor);

        ed1 = findViewById(R.id.ed1);
        lottie = findViewById(R.id.virtual_assistant_lottie_animation);
        lottie.setRepeatCount(LottieDrawable.INFINITE);
        lottie.playAnimation();

        //Virtual Assistant Section
        chatView = findViewById(R.id.chatView);
        editMessage = findViewById(R.id.editMessage);
        btnSend = findViewById(R.id.btnSend);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        AlertDialog.Builder va_guide_builder = new AlertDialog.Builder(this);
        builder.setTitle("PANGTOURISTA VIRTUAL ASSISTANT...");
        va_guide_builder.setTitle("VIRTUAL ASSISTANT GUIDE");
        View view = getLayoutInflater().inflate(R.layout.modal_main_product, null);
        View view_guide = getLayoutInflater().inflate(R.layout.modal_virtual_assistant_guide, null);
        //View dialog
        builder.setView(view);
        va_guide_builder.setView(view_guide);
        //Create dialog
        dialog = builder.create();
        va_guide_modal = va_guide_builder.create();
        mainProduct = view.findViewById(R.id.description);

        close_va = view.findViewById(R.id.close_va);
        close_va.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                t1.stop();
                dialog.dismiss();
            }
        });

        dialog.dismiss();
        va_guide_modal.dismiss();

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                // Stop the TTS when the dialog is dismissed
                t1.stop();
            }
        });


        chatAdapter = new ChatAdapter(messageList, this);
        chatView.setAdapter(chatAdapter);
        t1 = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if(i != TextToSpeech.ERROR)
                    t1.setLanguage(Locale.ENGLISH);
            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                String message = editMessage.getText().toString();
                if (!message.isEmpty()) {
                    messageList.add(new Message(message, false));
                    editMessage.setText("");
                    sendMessageToBot(message);
                    Objects.requireNonNull(chatView.getAdapter()).notifyDataSetChanged();
                    Objects.requireNonNull(chatView.getLayoutManager())
                            .scrollToPosition(messageList.size() - 1);
                } else {
                    Toast.makeText(VistualAssistantActivity.this, "Please enter text!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        setUpBot();

        lottie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Need To Speak");
                try {
                    startActivityForResult(intent, REQ_CODE);
                } catch (ActivityNotFoundException a) {
                    Toast.makeText(VistualAssistantActivity.this, "Sorry, Your Device not Supported", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void sendMessageToBot(String message) {
        QueryInput input = QueryInput.newBuilder()
                .setText(TextInput.newBuilder().setText(message).setLanguageCode("en-US")).build();
        new SendMessageInBg(this, sessionName, sessionsClient, input).execute();
    }

    @Override
    public void callback(DetectIntentResponse returnResponse) {
        if(returnResponse!=null) {
            String botReply = returnResponse.getQueryResult().getFulfillmentText();
            if(!botReply.isEmpty()){
                va_run_Function(botReply);
                messageList.add(new Message(botReply, true));
                chatAdapter.notifyDataSetChanged();
                Objects.requireNonNull(chatView.getLayoutManager()).scrollToPosition(messageList.size() - 1);
            }else {
                Toast.makeText(this, "something went wrong", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "failed to connect!", Toast.LENGTH_SHORT).show();
        }
    }

    private void va_run_Function(String botReply) {
//        Toast.makeText(this, botReply, Toast.LENGTH_SHORT).show();

        t1.speak(botReply, TextToSpeech.QUEUE_FLUSH, null);

        if (botReply.equals("I didn't get that. Can you say it again?")){
            t1.speak("Sorry, I didn't catch what you said. Here is the guide you can use to ask me", TextToSpeech.QUEUE_FLUSH, null);
            va_guide_modal.show();
        }
        if (botReply.equals("What was that?")){
            t1.speak("What was that? The only thing you can inquire about is this", TextToSpeech.QUEUE_FLUSH, null);
            va_guide_modal.show();
        }
        if (botReply.equals("Say that one more time?")){
            t1.speak("Could you please say that one more time? I will provide the guide for you to ask me", TextToSpeech.QUEUE_FLUSH, null);
            va_guide_modal.show();
        }

        //Start Nearby Places Section
        if (botReply.equals("these are the hospitals near your location that you can go to")){
            Intent intent = new Intent(getApplicationContext(), GetNearbyPlaces.class);
            intent.putExtra("place_type", "hospital");
            t1.speak(botReply, TextToSpeech.QUEUE_FLUSH, null);
            startActivity(intent);
        }
        if (botReply.equals("do you plan to refuel? Here are the gasoline stations near your location")){
            Intent intent = new Intent(getApplicationContext(), GetNearbyPlaces.class);
            intent.putExtra("place_type", "gas_station");
            startActivity(intent);
        }
        if (botReply.equals("these are the banks located near your current location")){
            Intent intent = new Intent(getApplicationContext(), GetNearbyPlaces.class);
            intent.putExtra("place_type", "bank");
            startActivity(intent);
        }
        if (botReply.equals("these are the police stations you can visit that are close to your location")){
            Intent intent = new Intent(getApplicationContext(), GetNearbyPlaces.class);
            intent.putExtra("place_type", "police");
            startActivity(intent);
        }
        if (botReply.equals("these are the restaurants conveniently located near your current location")){
            Intent intent = new Intent(getApplicationContext(), GetNearbyPlaces.class);
            intent.putExtra("place_type", "restaurant");
            startActivity(intent);
        }
        if (botReply.equals("here are the parking areas in close proximity to your current location")){
            Intent intent = new Intent(getApplicationContext(), GetNearbyPlaces.class);
            intent.putExtra("place_type", "park");
            startActivity(intent);
        }
        if (botReply.equals("these are the hotels situated close to your current location")){
            Intent intent = new Intent(getApplicationContext(), GetNearbyPlaces.class);
            intent.putExtra("place_type", "hotel");
            startActivity(intent);
        }
        if (botReply.equals("supermarket")){
            Intent intent = new Intent(getApplicationContext(), GetNearbyPlaces.class);
            intent.putExtra("place_type", "supermarket");
            startActivity(intent);
        }
        if (botReply.equals("repair shop")){
            Intent intent = new Intent(getApplicationContext(), GetNearbyPlaces.class);
            intent.putExtra("place_type", "car_repair");
            startActivity(intent);
        }
        if (botReply.equals("bus station")){
            Intent intent = new Intent(getApplicationContext(), GetNearbyPlaces.class);
            intent.putExtra("place_type", "bus_station");
            startActivity(intent);
        }

        if (botReply.equals("atm")){
            Intent intent = new Intent(getApplicationContext(), GetNearbyPlaces.class);
            intent.putExtra("place_type", "atm");
            startActivity(intent);
        }
        //End Nearby Places Section

        //Start Category display landmark or destination
        if (botReply.equals("here are the adventures we can try in pangasinan, so come and give them a shot!")){
            Intent intent = new Intent(getApplicationContext(), CategoryActivity.class);
            intent.putExtra("catId", 10);
            intent.putExtra("categoryName", "Adventure");
            startActivity(intent);
        }
        if (botReply.equals("here are the rich historical heritage and monuments that can be seen when visiting municipalities and cities in pangasinan")){
            Intent intent = new Intent(getApplicationContext(), CategoryActivity.class);
            intent.putExtra("catId", 11);
            intent.putExtra("categoryName", "Cultural Heritage");
            Animatoo.animateFade(VistualAssistantActivity.this);
            startActivity(intent);
        }


        if (botReply.equals("here are the stunning beaches waiting for you in Pangasinan; don't miss the chance to visit!")){
            Intent intent = new Intent(getApplicationContext(), CategoryActivity.class);
            intent.putExtra("catId", 3);
            intent.putExtra("categoryName", "Beach and Island");
            Animatoo.animateFade(VistualAssistantActivity.this);
            startActivity(intent);
        }

        if (botReply.equals("these are the ridges and reefs that pangasinan has")){
            Intent intent = new Intent(getApplicationContext(), CategoryActivity.class);
            intent.putExtra("catId", 12);
            intent.putExtra("categoryName", "Ridge and Reef");
            Animatoo.animateFade(VistualAssistantActivity.this);
            startActivity(intent);
        }

        if (botReply.equals("these are for the family")){
            Intent intent = new Intent(getApplicationContext(), CategoryActivity.class);
            intent.putExtra("catId", 12);
            intent.putExtra("categoryName", "Family");
            Animatoo.animateFade(VistualAssistantActivity.this);
            startActivity(intent);
        }

        if (botReply.equals("these are the products you can discover in Pangasinan; come and visit now!")){
            Intent intent = new Intent(getApplicationContext(), CategoryActivity.class);
            intent.putExtra("categoryName", "Product");
            Animatoo.animateFade(VistualAssistantActivity.this);
            startActivity(intent);
        }

        if (botReply.equals("these are the pilgrimage sites or churches you can visit in Pangasinan")){
            Intent intent = new Intent(getApplicationContext(), CategoryActivity.class);
            intent.putExtra("catId", 12);
            intent.putExtra("categoryName", "Pilgrimage");
            Animatoo.animateFade(VistualAssistantActivity.this);
            startActivity(intent);
        }

        if (botReply.equals("these are the other things/places that can be seen in pangasinan")){
            Intent intent = new Intent(getApplicationContext(), CategoryActivity.class);
            intent.putExtra("catId", 12);
            intent.putExtra("categoryName", "Other");
            Animatoo.animateFade(VistualAssistantActivity.this);
            startActivity(intent);
        }

        if (botReply.equals("these are the farms and natural attractions we can visit in Pangasinan, so come and explore!")){
            Intent intent = new Intent(getApplicationContext(), CategoryActivity.class);
            intent.putExtra("catId", 4);
            intent.putExtra("categoryName", "Farm and Nature");
            Animatoo.animateFade(VistualAssistantActivity.this);
            startActivity(intent);
        }
        //End Category display landmark or destination

        //Start Latest news
        if (botReply.equals("These represent a diverse array of news articles and events, all of which are currently unfolding in the province of Pangasinan.")){
            Intent intent = new Intent(getApplicationContext(), NewsEventActivity.class);
            Animatoo.animateFade(VistualAssistantActivity.this);
            startActivity(intent);
        }
        //End Latest news

//Start Municipality History/Discription
        //-------------------------------------------------------------------------------------
        if (botReply.equals("history of agno")){
            Intent intent = new Intent(getApplicationContext(), MunicipalityDetailActivity.class);
            intent.putExtra("municipality_id", 31);
            intent.putExtra("speak_history", "true");
            Animatoo.animateFade(VistualAssistantActivity.this);
            startActivity(intent);
        }

        if (botReply.equals("history of aguilar")){
            Intent intent = new Intent(getApplicationContext(), MunicipalityDetailActivity.class);
            intent.putExtra("municipality_id", 69);
            intent.putExtra("speak_history", "true");
            Animatoo.animateFade(VistualAssistantActivity.this);
            startActivity(intent);
        }

        if (botReply.equals("history of alaminos city")){
            Intent intent = new Intent(getApplicationContext(), MunicipalityDetailActivity.class);
            intent.putExtra("municipality_id", 44);
            intent.putExtra("speak_history", "true");
            Animatoo.animateFade(VistualAssistantActivity.this);
            startActivity(intent);
        }

        if (botReply.equals("history of alcala")){
            Intent intent = new Intent(getApplicationContext(), MunicipalityDetailActivity.class);
            intent.putExtra("municipality_id", 75);
            intent.putExtra("speak_history", "true");
            Animatoo.animateFade(VistualAssistantActivity.this);
            startActivity(intent);
        }

        if (botReply.equals("history of anda")){
            Intent intent = new Intent(getApplicationContext(), MunicipalityDetailActivity.class);
            intent.putExtra("municipality_id", 55);
            intent.putExtra("speak_history", "true");
            Animatoo.animateFade(VistualAssistantActivity.this);
            startActivity(intent);
        }

        if (botReply.equals("history of asingan")){
            Intent intent = new Intent(getApplicationContext(), MunicipalityDetailActivity.class);
            intent.putExtra("municipality_id", 74);
            intent.putExtra("speak_history", "true");
            Animatoo.animateFade(VistualAssistantActivity.this);
            startActivity(intent);
        }

        if (botReply.equals("history of balungao")){
            Intent intent = new Intent(getApplicationContext(), MunicipalityDetailActivity.class);
            intent.putExtra("municipality_id", 68);
            intent.putExtra("speak_history", "true");
            Animatoo.animateFade(VistualAssistantActivity.this);
            startActivity(intent);
        }

        if (botReply.equals("history of bani")){
            Intent intent = new Intent(getApplicationContext(), MunicipalityDetailActivity.class);
            intent.putExtra("municipality_id", 33);
            intent.putExtra("speak_history", "true");
            Animatoo.animateFade(VistualAssistantActivity.this);
            startActivity(intent);
        }


        if (botReply.equals("history of basista")){
            Intent intent = new Intent(getApplicationContext(), MunicipalityDetailActivity.class);
            intent.putExtra("municipality_id", 57);
            intent.putExtra("speak_history", "true");
            Animatoo.animateFade(VistualAssistantActivity.this);
            startActivity(intent);
        }

        if (botReply.equals("history of bautista")){
            Intent intent = new Intent(getApplicationContext(), MunicipalityDetailActivity.class);
            intent.putExtra("municipality_id", 70);
            intent.putExtra("speak_history", "true");
            Animatoo.animateFade(VistualAssistantActivity.this);
            startActivity(intent);
        }

        if (botReply.equals("history of bayambang")){
            Intent intent = new Intent(getApplicationContext(), MunicipalityDetailActivity.class);
            intent.putExtra("municipality_id", 64);
            intent.putExtra("speak_history", "true");
            Animatoo.animateFade(VistualAssistantActivity.this);
            startActivity(intent);
        }

        if (botReply.equals("history of binalonan")){
            Intent intent = new Intent(getApplicationContext(), MunicipalityDetailActivity.class);
            intent.putExtra("municipality_id", 54);
            intent.putExtra("speak_history", "true");
            Animatoo.animateFade(VistualAssistantActivity.this);
            startActivity(intent);
        }

        if (botReply.equals("history of binmaley")){
            Intent intent = new Intent(getApplicationContext(), MunicipalityDetailActivity.class);
            intent.putExtra("municipality_id", 40);
            intent.putExtra("speak_history", "true");
            Animatoo.animateFade(VistualAssistantActivity.this);
            startActivity(intent);
        }

        if (botReply.equals("history of bolinao")){
            Intent intent = new Intent(getApplicationContext(), MunicipalityDetailActivity.class);
            intent.putExtra("municipality_id", 32);
            intent.putExtra("speak_history", "true");
            Animatoo.animateFade(VistualAssistantActivity.this);
            startActivity(intent);
        }

        if (botReply.equals("history of bugallon")){
            Intent intent = new Intent(getApplicationContext(), MunicipalityDetailActivity.class);
            intent.putExtra("municipality_id", 43);
            intent.putExtra("speak_history", "true");
            Animatoo.animateFade(VistualAssistantActivity.this);
            startActivity(intent);
        }

        if (botReply.equals("history of burgos")){
            Intent intent = new Intent(getApplicationContext(), MunicipalityDetailActivity.class);
            intent.putExtra("municipality_id", 35);
            intent.putExtra("speak_history", "true");
            Animatoo.animateFade(VistualAssistantActivity.this);
            startActivity(intent);
        }

        if (botReply.equals("history of calasiao")){
            Intent intent = new Intent(getApplicationContext(), MunicipalityDetailActivity.class);
            intent.putExtra("municipality_id", 30);
            intent.putExtra("speak_history", "true");
            Animatoo.animateFade(VistualAssistantActivity.this);
            startActivity(intent);
        }

        if (botReply.equals("history of dagupan city")){
            Intent intent = new Intent(getApplicationContext(), MunicipalityDetailActivity.class);
            intent.putExtra("municipality_id", 47);
            intent.putExtra("speak_history", "true");
            Animatoo.animateFade(VistualAssistantActivity.this);
            startActivity(intent);
        }

        if (botReply.equals("history of dasol")){
            Intent intent = new Intent(getApplicationContext(), MunicipalityDetailActivity.class);
            intent.putExtra("municipality_id", 36);
            intent.putExtra("speak_history", "true");
            Animatoo.animateFade(VistualAssistantActivity.this);
            startActivity(intent);
        }

        if (botReply.equals("history of infanta")){
            Intent intent = new Intent(getApplicationContext(), MunicipalityDetailActivity.class);
            intent.putExtra("municipality_id", 37);
            intent.putExtra("speak_history", "true");
            Animatoo.animateFade(VistualAssistantActivity.this);
            startActivity(intent);
        }

        if (botReply.equals("history of labrador")){
            Intent intent = new Intent(getApplicationContext(), MunicipalityDetailActivity.class);
            intent.putExtra("municipality_id", 38);
            intent.putExtra("speak_history", "true");
            Animatoo.animateFade(VistualAssistantActivity.this);
            startActivity(intent);
        }

        if (botReply.equals("history of laoac")){
            Intent intent = new Intent(getApplicationContext(), MunicipalityDetailActivity.class);
            intent.putExtra("municipality_id", 60);
            intent.putExtra("speak_history", "true");
            Animatoo.animateFade(VistualAssistantActivity.this);
            startActivity(intent);
        }

        if (botReply.equals("history of lingayen")){
            Intent intent = new Intent(getApplicationContext(), MunicipalityDetailActivity.class);
            intent.putExtra("municipality_id", 39);
            intent.putExtra("speak_history", "true");
            Animatoo.animateFade(VistualAssistantActivity.this);
            startActivity(intent);
        }

        if (botReply.equals("history of mabini")){
            Intent intent = new Intent(getApplicationContext(), MunicipalityDetailActivity.class);
            intent.putExtra("municipality_id", 34);
            intent.putExtra("speak_history", "true");
            Animatoo.animateFade(VistualAssistantActivity.this);
            startActivity(intent);
        }

        if (botReply.equals("history of malasiqui")){
            Intent intent = new Intent(getApplicationContext(), MunicipalityDetailActivity.class);
            intent.putExtra("municipality_id", 65);
            intent.putExtra("speak_history", "true");
            Animatoo.animateFade(VistualAssistantActivity.this);
            startActivity(intent);
        }

        if (botReply.equals("history of manaoag")){
            Intent intent = new Intent(getApplicationContext(), MunicipalityDetailActivity.class);
            intent.putExtra("municipality_id", 66);
            intent.putExtra("speak_history", "true");
            Animatoo.animateFade(VistualAssistantActivity.this);
            startActivity(intent);
        }

        if (botReply.equals("history of mangaldan")){
            Intent intent = new Intent(getApplicationContext(), MunicipalityDetailActivity.class);
            intent.putExtra("municipality_id", 76);
            intent.putExtra("speak_history", "true");
            Animatoo.animateFade(VistualAssistantActivity.this);
            startActivity(intent);
        }

        if (botReply.equals("history of mangatarem")){
            Intent intent = new Intent(getApplicationContext(), MunicipalityDetailActivity.class);
            intent.putExtra("municipality_id", 46);
            intent.putExtra("speak_history", "true");
            Animatoo.animateFade(VistualAssistantActivity.this);
            startActivity(intent);
        }

        if (botReply.equals("history of mapandan")){
            Intent intent = new Intent(getApplicationContext(), MunicipalityDetailActivity.class);
            intent.putExtra("municipality_id", 48);
            intent.putExtra("speak_history", "true");
            Animatoo.animateFade(VistualAssistantActivity.this);
            startActivity(intent);
        }

        if (botReply.equals("history of natividad")){
            Intent intent = new Intent(getApplicationContext(), MunicipalityDetailActivity.class);
            intent.putExtra("municipality_id", 71);
            intent.putExtra("speak_history", "true");
            Animatoo.animateFade(VistualAssistantActivity.this);
            startActivity(intent);
        }

        if (botReply.equals("history of pozorrubio")){
            Intent intent = new Intent(getApplicationContext(), MunicipalityDetailActivity.class);
            intent.putExtra("municipality_id", 72);
            intent.putExtra("speak_history", "true");
            Animatoo.animateFade(VistualAssistantActivity.this);
            startActivity(intent);
        }

        if (botReply.equals("history of province of Pangasinan")){
            Intent intent = new Intent(getApplicationContext(), MunicipalityDetailActivity.class);
            intent.putExtra("municipality_id", 89);
            intent.putExtra("speak_history", "true");
            Animatoo.animateFade(VistualAssistantActivity.this);
            startActivity(intent);
        }

        if (botReply.equals("history of rosales")){
            Intent intent = new Intent(getApplicationContext(), MunicipalityDetailActivity.class);
            intent.putExtra("municipality_id", 61);
            intent.putExtra("speak_history", "true");
            Animatoo.animateFade(VistualAssistantActivity.this);
            startActivity(intent);
        }

        if (botReply.equals("history of san carlos city")){
            Intent intent = new Intent(getApplicationContext(), MunicipalityDetailActivity.class);
            intent.putExtra("municipality_id", 73);
            intent.putExtra("speak_history", "true");
            Animatoo.animateFade(VistualAssistantActivity.this);
            startActivity(intent);
        }

        if (botReply.equals("history of san fabian")){
            Intent intent = new Intent(getApplicationContext(), MunicipalityDetailActivity.class);
            intent.putExtra("municipality_id", 63);
            intent.putExtra("speak_history", "true");
            Animatoo.animateFade(VistualAssistantActivity.this);
            startActivity(intent);
        }

        if (botReply.equals("history of san jacinto")){
            Intent intent = new Intent(getApplicationContext(), MunicipalityDetailActivity.class);
            intent.putExtra("municipality_id", 52);
            intent.putExtra("speak_history", "true");
            Animatoo.animateFade(VistualAssistantActivity.this);
            startActivity(intent);
        }

        if (botReply.equals("history of san manuel")){
            Intent intent = new Intent(getApplicationContext(), MunicipalityDetailActivity.class);
            intent.putExtra("municipality_id", 58);
            intent.putExtra("speak_history", "true");
            Animatoo.animateFade(VistualAssistantActivity.this);
            startActivity(intent);
        }

        if (botReply.equals("history of san nicolas")){
            Intent intent = new Intent(getApplicationContext(), MunicipalityDetailActivity.class);
            intent.putExtra("municipality_id", 53);
            intent.putExtra("speak_history", "true");
            Animatoo.animateFade(VistualAssistantActivity.this);
            startActivity(intent);
        }

        if (botReply.equals("history of san quintin")){
            Intent intent = new Intent(getApplicationContext(), MunicipalityDetailActivity.class);
            intent.putExtra("municipality_id", 59);
            intent.putExtra("speak_history", "true");
            Animatoo.animateFade(VistualAssistantActivity.this);
            startActivity(intent);
        }

        if (botReply.equals("history of santa barbara")){
            Intent intent = new Intent(getApplicationContext(), MunicipalityDetailActivity.class);
            intent.putExtra("municipality_id", 56);
            intent.putExtra("speak_history", "true");
            Animatoo.animateFade(VistualAssistantActivity.this);
            startActivity(intent);
        }

        if (botReply.equals("history of santa maria")){
            Intent intent = new Intent(getApplicationContext(), MunicipalityDetailActivity.class);
            intent.putExtra("municipality_id", 78);
            intent.putExtra("speak_history", "true");
            Animatoo.animateFade(VistualAssistantActivity.this);
            startActivity(intent);
        }

        if (botReply.equals("history of santo tomas")){
            Intent intent = new Intent(getApplicationContext(), MunicipalityDetailActivity.class);
            intent.putExtra("municipality_id", 77);
            intent.putExtra("speak_history", "true");
            Animatoo.animateFade(VistualAssistantActivity.this);
            startActivity(intent);
        }

        if (botReply.equals("history of sison")){
            Intent intent = new Intent(getApplicationContext(), MunicipalityDetailActivity.class);
            intent.putExtra("municipality_id", 51);
            intent.putExtra("speak_history", "true");
            Animatoo.animateFade(VistualAssistantActivity.this);
            startActivity(intent);
        }

        if (botReply.equals("history of sual")){
            Intent intent = new Intent(getApplicationContext(), MunicipalityDetailActivity.class);
            intent.putExtra("municipality_id", 45);
            intent.putExtra("speak_history", "true");
            Animatoo.animateFade(VistualAssistantActivity.this);
            startActivity(intent);
        }

        if (botReply.equals("history of tayug")){
            Intent intent = new Intent(getApplicationContext(), MunicipalityDetailActivity.class);
            intent.putExtra("municipality_id", 49);
            intent.putExtra("speak_history", "true");
            Animatoo.animateFade(VistualAssistantActivity.this);
            startActivity(intent);
        }

        if (botReply.equals("history of umingan")){
            Intent intent = new Intent(getApplicationContext(), MunicipalityDetailActivity.class);
            intent.putExtra("municipality_id", 50);
            intent.putExtra("speak_history", "true");
            Animatoo.animateFade(VistualAssistantActivity.this);
            startActivity(intent);
        }

        if (botReply.equals("history of urbiztondo")){
            Intent intent = new Intent(getApplicationContext(), MunicipalityDetailActivity.class);
            intent.putExtra("municipality_id", 67);
            intent.putExtra("speak_history", "true");
            Animatoo.animateFade(VistualAssistantActivity.this);
            startActivity(intent);
        }

        if (botReply.equals("history of urdaneta city")){
            Intent intent = new Intent(getApplicationContext(), MunicipalityDetailActivity.class);
            intent.putExtra("municipality_id", 95);
            intent.putExtra("speak_history", "true");
            Animatoo.animateFade(VistualAssistantActivity.this);
            startActivity(intent);
        }

        if (botReply.equals("history of villasis")){
            Intent intent = new Intent(getApplicationContext(), MunicipalityDetailActivity.class);
            intent.putExtra("municipality_id", 62);
            intent.putExtra("speak_history", "true");
            Animatoo.animateFade(VistualAssistantActivity.this);
            startActivity(intent);
        }




        //Municipality/City Main product
        if (botReply.equals("the proudly acclaimed main product of agno")){
            int municipality_id = 31;
            getMainProduct(municipality_id);
        }

        if (botReply.equals("the proudly acclaimed main product of aguilar")){
            int municipality_id = 69;
            getMainProduct(municipality_id);
        }

        if (botReply.equals("the proudly acclaimed main product of alaminos city")){
            int municipality_id = 44;
            getMainProduct(municipality_id);
        }

        if (botReply.equals("the proudly acclaimed main product of alcala")){
            int municipality_id = 75;
            getMainProduct(municipality_id);
        }

        if (botReply.equals("the proudly acclaimed main product of anda")){
            int municipality_id = 55;
            getMainProduct(municipality_id);
        }

        if (botReply.equals("the proudly acclaimed main product of asingan")){
            int municipality_id = 74;
            getMainProduct(municipality_id);
        }

        if (botReply.equals("the proudly acclaimed main product of balungao")){
            int municipality_id = 68;
            getMainProduct(municipality_id);
        }

        if (botReply.equals("the proudly acclaimed main product of bani")){
            int municipality_id = 33;
            getMainProduct(municipality_id);
        }


        if (botReply.equals("the proudly acclaimed main product of basista")){
            int municipality_id = 57;
            getMainProduct(municipality_id);
        }

        if (botReply.equals("the proudly acclaimed main product of bautista")){
            int municipality_id = 70;
            getMainProduct(municipality_id);
        }

        if (botReply.equals("the proudly acclaimed main product of bayambang")){
            int municipality_id = 64;
            getMainProduct(municipality_id);
        }

        if (botReply.equals("the proudly acclaimed main product of binalonan")){
            int municipality_id = 54;
            getMainProduct(municipality_id);
        }

        if (botReply.equals("the proudly acclaimed main product of binmaley")){
            int municipality_id = 40;
            getMainProduct(municipality_id);
        }

        if (botReply.equals("the proudly acclaimed main product of bolinao")){
            int municipality_id = 32;
            getMainProduct(municipality_id);
        }

        if (botReply.equals("the proudly acclaimed main product of bugallon")){
            int municipality_id = 43;
            getMainProduct(municipality_id);
        }

        if (botReply.equals("the proudly acclaimed main product of burgos")){
            int municipality_id = 35;
            getMainProduct(municipality_id);
        }

        if (botReply.equals("the proudly acclaimed main product of calasiao")){
            int municipality_id = 30;
            getMainProduct(municipality_id);
        }

        if (botReply.equals("the proudly acclaimed main product of dagupan city")){
            int municipality_id = 47;
            getMainProduct(municipality_id);
        }

        if (botReply.equals("the proudly acclaimed main product of dasol")){
            int municipality_id = 36;
            getMainProduct(municipality_id);
        }

        if (botReply.equals("the proudly acclaimed main product of infanta")){
            int municipality_id = 37;
            getMainProduct(municipality_id);
        }

        if (botReply.equals("the proudly acclaimed main product of labrador")){
            int municipality_id = 38;
            getMainProduct(municipality_id);
        }

        if (botReply.equals("the proudly acclaimed main product of laoac")){
            int municipality_id = 60;
            getMainProduct(municipality_id);
        }

        if (botReply.equals("the proudly acclaimed main product of lingayen")){
            int municipality_id = 39;
            getMainProduct(municipality_id);
        }

        if (botReply.equals("the proudly acclaimed main product of mabini")){
            int municipality_id = 34;
            getMainProduct(municipality_id);
        }

        if (botReply.equals("the proudly acclaimed main product of malasiqui")){
            int municipality_id = 65;
            getMainProduct(municipality_id);
        }

        if (botReply.equals("the proudly acclaimed main product of manaoag")){
            int municipality_id = 66;
            getMainProduct(municipality_id);
        }

        if (botReply.equals("the proudly acclaimed main product of mangaldan")){
            int municipality_id = 76;
            getMainProduct(municipality_id);
        }

        if (botReply.equals("the proudly acclaimed main product of mangatarem")){
            int municipality_id = 46;
            getMainProduct(municipality_id);
        }

        if (botReply.equals("the proudly acclaimed main product of mapandan")){
            int municipality_id = 48;
            getMainProduct(municipality_id);
        }

        if (botReply.equals("the proudly acclaimed main product of natividad")){
            int municipality_id = 71;
            getMainProduct(municipality_id);
        }

        if (botReply.equals("the proudly acclaimed main product of pozorrubio")){
            int municipality_id = 72;
            getMainProduct(municipality_id);
        }

        if (botReply.equals("the proudly acclaimed main product of Pangasinan")){
            int municipality_id = 89;
            getMainProduct(municipality_id);
        }

        if (botReply.equals("the proudly acclaimed main product of rosales")){
            int municipality_id = 61;
            getMainProduct(municipality_id);
        }

        if (botReply.equals("the proudly acclaimed main product of san carlos city")){
            int municipality_id = 73;
            getMainProduct(municipality_id);
        }

        if (botReply.equals("the proudly acclaimed main product of san fabian")){
            int municipality_id = 63;
            getMainProduct(municipality_id);
        }

        if (botReply.equals("the proudly acclaimed main product of san jacinto")){
            int municipality_id = 52;
            getMainProduct(municipality_id);
        }

        if (botReply.equals("the proudly acclaimed main product of san manuel")){
            int municipality_id = 58;
            getMainProduct(municipality_id);
        }

        if (botReply.equals("the proudly acclaimed main product of san nicolas")){
            int municipality_id = 53;
            getMainProduct(municipality_id);
        }

        if (botReply.equals("the proudly acclaimed main product of san quintin")){
            int municipality_id = 59;
            getMainProduct(municipality_id);
        }

        if (botReply.equals("the proudly acclaimed main product of santa barbara")){
            int municipality_id = 56;
            getMainProduct(municipality_id);
        }

        if (botReply.equals("the proudly acclaimed main product of santa maria")){
            int municipality_id = 78;
            getMainProduct(municipality_id);
        }

        if (botReply.equals("the proudly acclaimed main product of santo tomas")){
            int municipality_id = 77;
            getMainProduct(municipality_id);
        }

        if (botReply.equals("the proudly acclaimed main product of sison")){
            int municipality_id = 51;
            getMainProduct(municipality_id);
        }

        if (botReply.equals("the proudly acclaimed main product of sual")){
            int municipality_id = 45;
            getMainProduct(municipality_id);
        }

        if (botReply.equals("the proudly acclaimed main product of tayug")){
            int municipality_id = 49;
            getMainProduct(municipality_id);
        }

        if (botReply.equals("the proudly acclaimed main product of umingan")){
            int municipality_id = 50;
            getMainProduct(municipality_id);
        }

        if (botReply.equals("the proudly acclaimed main product of urbiztondo")){
            int municipality_id = 67;
            getMainProduct(municipality_id);
        }


        if (botReply.equals("the proudly acclaimed main product of Urdaneta City")){
            int municipality_id = 95;
            getMainProduct(municipality_id);
        }

        if (botReply.equals("the proudly acclaimed main product of villasis")){
            int municipality_id = 62;
            getMainProduct(municipality_id);
        }


        //Start Attraction per municipality
        if (botReply.equals("these are the things/places that we can see in agno")){
            Intent intent = new Intent(getApplicationContext(), SeeMunicipalityActivity.class);
            intent.putExtra("municipality_id", 31);
            Animatoo.animateFade(VistualAssistantActivity.this);
            startActivity(intent);
        }

        if (botReply.equals("these are the things/places that we can see in aguilar")){
            Intent intent = new Intent(getApplicationContext(), SeeMunicipalityActivity.class);
            intent.putExtra("municipality_id", 69);
            Animatoo.animateFade(VistualAssistantActivity.this);
            startActivity(intent);
        }

        if (botReply.equals("these are the things/places that we can see in alaminos city")){
            Intent intent = new Intent(getApplicationContext(), SeeMunicipalityActivity.class);
            intent.putExtra("municipality_id", 44);
            Animatoo.animateFade(VistualAssistantActivity.this);
            startActivity(intent);
        }

        if (botReply.equals("these are the things/places that we can see in alcala")){
            Intent intent = new Intent(getApplicationContext(), SeeMunicipalityActivity.class);
            intent.putExtra("municipality_id", 75);
            Animatoo.animateFade(VistualAssistantActivity.this);
            startActivity(intent);
        }

        if (botReply.equals("these are the things/places that we can see in anda")){
            Intent intent = new Intent(getApplicationContext(), SeeMunicipalityActivity.class);
            intent.putExtra("municipality_id", 55);
            Animatoo.animateFade(VistualAssistantActivity.this);
            startActivity(intent);
        }

        if (botReply.equals("these are the things/places that we can see in asingan")){
            Intent intent = new Intent(getApplicationContext(), SeeMunicipalityActivity.class);
            intent.putExtra("municipality_id", 74);
            Animatoo.animateFade(VistualAssistantActivity.this);
            startActivity(intent);
        }

        if (botReply.equals("these are the things/places that we can see in balungao")){
            Intent intent = new Intent(getApplicationContext(), SeeMunicipalityActivity.class);
            intent.putExtra("municipality_id", 68);
            Animatoo.animateFade(VistualAssistantActivity.this);
            startActivity(intent);
        }

        if (botReply.equals("these are the things/places that we can see in bani")){
            Intent intent = new Intent(getApplicationContext(), SeeMunicipalityActivity.class);
            intent.putExtra("municipality_id", 33);
            Animatoo.animateFade(VistualAssistantActivity.this);
            startActivity(intent);
        }

        if (botReply.equals("these are the things/places that we can see in basista")){
            Intent intent = new Intent(getApplicationContext(), SeeMunicipalityActivity.class);
            intent.putExtra("municipality_id", 57);
            Animatoo.animateFade(VistualAssistantActivity.this);
            startActivity(intent);
        }

        if (botReply.equals("these are the things/places that we can see in bautista")){
            Intent intent = new Intent(getApplicationContext(), SeeMunicipalityActivity.class);
            intent.putExtra("municipality_id", 70);
            Animatoo.animateFade(VistualAssistantActivity.this);
            startActivity(intent);
        }

        if (botReply.equals("these are the things/places that we can see in bayambang")){
            Intent intent = new Intent(getApplicationContext(), SeeMunicipalityActivity.class);
            intent.putExtra("municipality_id", 64);
            Animatoo.animateFade(VistualAssistantActivity.this);
            startActivity(intent);
        }

        if (botReply.equals("these are the things/places that we can see in binalonan")){
            Intent intent = new Intent(getApplicationContext(), SeeMunicipalityActivity.class);
            intent.putExtra("municipality_id", 54);
            Animatoo.animateFade(VistualAssistantActivity.this);
            startActivity(intent);
        }

        if (botReply.equals("these are the things/places that we can see in binmaley")){
            Intent intent = new Intent(getApplicationContext(), SeeMunicipalityActivity.class);
            intent.putExtra("municipality_id", 40);
            Animatoo.animateFade(VistualAssistantActivity.this);
            startActivity(intent);
        }

        if (botReply.equals("these are the things/places that we can see in bolinao")){
            Intent intent = new Intent(getApplicationContext(), SeeMunicipalityActivity.class);
            intent.putExtra("municipality_id", 32);
            Animatoo.animateFade(VistualAssistantActivity.this);
            startActivity(intent);
        }

        if (botReply.equals("these are the things/places that we can see in bugallon")){
            Intent intent = new Intent(getApplicationContext(), SeeMunicipalityActivity.class);
            intent.putExtra("municipality_id", 43);
            Animatoo.animateFade(VistualAssistantActivity.this);
            startActivity(intent);
        }

        if (botReply.equals("these are the things/places that we can see in burgos")){
            Intent intent = new Intent(getApplicationContext(), SeeMunicipalityActivity.class);
            intent.putExtra("municipality_id", 35);
            Animatoo.animateFade(VistualAssistantActivity.this);
            startActivity(intent);
        }

        if (botReply.equals("these are the things/places that we can see in calasiao")){
            Intent intent = new Intent(getApplicationContext(), SeeMunicipalityActivity.class);
            intent.putExtra("municipality_id", 30);
            Animatoo.animateFade(VistualAssistantActivity.this);
            startActivity(intent);
        }

        if (botReply.equals("these are the things/places that we can see in dagupan city")){
            Intent intent = new Intent(getApplicationContext(), SeeMunicipalityActivity.class);
            intent.putExtra("municipality_id", 47);
            Animatoo.animateFade(VistualAssistantActivity.this);
            startActivity(intent);
        }

        if (botReply.equals("these are the things/places that we can see in dasol")){
            Intent intent = new Intent(getApplicationContext(), SeeMunicipalityActivity.class);
            intent.putExtra("municipality_id", 36);
            Animatoo.animateFade(VistualAssistantActivity.this);
            startActivity(intent);
        }

        if (botReply.equals("these are the things/places that we can see in infanta")){
            Intent intent = new Intent(getApplicationContext(), SeeMunicipalityActivity.class);
            intent.putExtra("municipality_id", 37);
            Animatoo.animateFade(VistualAssistantActivity.this);
            startActivity(intent);
        }

        if (botReply.equals("these are the things/places that we can see in labrador")){
            Intent intent = new Intent(getApplicationContext(), SeeMunicipalityActivity.class);
            intent.putExtra("municipality_id", 38);
            Animatoo.animateFade(VistualAssistantActivity.this);
            startActivity(intent);
        }

        if (botReply.equals("these are the things/places that we can see in laoac")){
            Intent intent = new Intent(getApplicationContext(), SeeMunicipalityActivity.class);
            intent.putExtra("municipality_id", 60);
            Animatoo.animateFade(VistualAssistantActivity.this);
            startActivity(intent);
        }

        if (botReply.equals("these are the things/places that we can see in lingayen")){
            Intent intent = new Intent(getApplicationContext(), SeeMunicipalityActivity.class);
            intent.putExtra("municipality_id", 39);
            Animatoo.animateFade(VistualAssistantActivity.this);
            startActivity(intent);
        }

        if (botReply.equals("these are the things/places that we can see in mabini")){
            Intent intent = new Intent(getApplicationContext(), SeeMunicipalityActivity.class);
            intent.putExtra("municipality_id", 34);
            Animatoo.animateFade(VistualAssistantActivity.this);
            startActivity(intent);
        }

        if (botReply.equals("these are the things/places that we can see in malasiqui")){
            Intent intent = new Intent(getApplicationContext(), SeeMunicipalityActivity.class);
            intent.putExtra("municipality_id", 65);
            Animatoo.animateFade(VistualAssistantActivity.this);
            startActivity(intent);
        }

        if (botReply.equals("these are the things/places that we can see in manaoag")){
            Intent intent = new Intent(getApplicationContext(), SeeMunicipalityActivity.class);
            intent.putExtra("municipality_id", 66);
            Animatoo.animateFade(VistualAssistantActivity.this);
            startActivity(intent);
        }

        if (botReply.equals("these are the things/places that we can see in mangaldan")){
            Intent intent = new Intent(getApplicationContext(), SeeMunicipalityActivity.class);
            intent.putExtra("municipality_id", 76);
            Animatoo.animateFade(VistualAssistantActivity.this);
            startActivity(intent);
        }

        if (botReply.equals("these are the things/places that we can see in mangatarem")){
            Intent intent = new Intent(getApplicationContext(), SeeMunicipalityActivity.class);
            intent.putExtra("municipality_id", 46);
            Animatoo.animateFade(VistualAssistantActivity.this);
            startActivity(intent);
        }

        if (botReply.equals("these are the things/places that we can see in mapandan")){
            Intent intent = new Intent(getApplicationContext(), SeeMunicipalityActivity.class);
            intent.putExtra("municipality_id", 48);
            Animatoo.animateFade(VistualAssistantActivity.this);
            startActivity(intent);
        }

        if (botReply.equals("these are the things/places that we can see in natividad")){
            Intent intent = new Intent(getApplicationContext(), SeeMunicipalityActivity.class);
            intent.putExtra("municipality_id", 71);
            Animatoo.animateFade(VistualAssistantActivity.this);
            startActivity(intent);
        }

        if (botReply.equals("these are the things/places that we can see in pozorrubio")){
            Intent intent = new Intent(getApplicationContext(), SeeMunicipalityActivity.class);
            intent.putExtra("municipality_id", 72);
            Animatoo.animateFade(VistualAssistantActivity.this);
            startActivity(intent);
        }

        if (botReply.equals("these are the things/places that we can see in rosales")){
            Intent intent = new Intent(getApplicationContext(), SeeMunicipalityActivity.class);
            intent.putExtra("municipality_id", 61);
            Animatoo.animateFade(VistualAssistantActivity.this);
            startActivity(intent);
        }

        if (botReply.equals("these are the things/places that we can see in san carlos city")){
            Intent intent = new Intent(getApplicationContext(), SeeMunicipalityActivity.class);
            intent.putExtra("municipality_id", 73);
            Animatoo.animateFade(VistualAssistantActivity.this);
            startActivity(intent);
        }

        if (botReply.equals("these are the things/places that we can see in san fabian")){
            Intent intent = new Intent(getApplicationContext(), SeeMunicipalityActivity.class);
            intent.putExtra("municipality_id", 63);
            Animatoo.animateFade(VistualAssistantActivity.this);
            startActivity(intent);
        }

        if (botReply.equals("these are the things/places that we can see in san jacinto")){
            Intent intent = new Intent(getApplicationContext(), SeeMunicipalityActivity.class);
            intent.putExtra("municipality_id", 52);
            Animatoo.animateFade(VistualAssistantActivity.this);
            startActivity(intent);
        }

        if (botReply.equals("these are the things/places that we can see in san manuel")){
            Intent intent = new Intent(getApplicationContext(), SeeMunicipalityActivity.class);
            intent.putExtra("municipality_id", 58);
            Animatoo.animateFade(VistualAssistantActivity.this);
            startActivity(intent);
        }

        if (botReply.equals("these are the things/places that we can see in san nicolas")){
            Intent intent = new Intent(getApplicationContext(), SeeMunicipalityActivity.class);
            intent.putExtra("municipality_id", 53);
            Animatoo.animateFade(VistualAssistantActivity.this);
            startActivity(intent);
        }

        if (botReply.equals("these are the things/places that we can see in san quintin")){
            Intent intent = new Intent(getApplicationContext(), SeeMunicipalityActivity.class);
            intent.putExtra("municipality_id", 59);
            Animatoo.animateFade(VistualAssistantActivity.this);
            startActivity(intent);
        }

        if (botReply.equals("these are the things/places that we can see in santa barbara")){
            Intent intent = new Intent(getApplicationContext(), SeeMunicipalityActivity.class);
            intent.putExtra("municipality_id", 56);
            Animatoo.animateFade(VistualAssistantActivity.this);
            startActivity(intent);
        }

        if (botReply.equals("these are the things/places that we can see in santa maria")){
            Intent intent = new Intent(getApplicationContext(), SeeMunicipalityActivity.class);
            intent.putExtra("municipality_id", 78);
            Animatoo.animateFade(VistualAssistantActivity.this);
            startActivity(intent);
        }

        if (botReply.equals("these are the things/places that we can see in santo tomas")){
            Intent intent = new Intent(getApplicationContext(), SeeMunicipalityActivity.class);
            intent.putExtra("municipality_id", 77);
            Animatoo.animateFade(VistualAssistantActivity.this);
            startActivity(intent);
        }

        if (botReply.equals("these are the things/places that we can see in sison")){
            Intent intent = new Intent(getApplicationContext(), SeeMunicipalityActivity.class);
            intent.putExtra("municipality_id", 51);
            Animatoo.animateFade(VistualAssistantActivity.this);
            startActivity(intent);
        }

        if (botReply.equals("these are the things/places that we can see in sual")){
            Intent intent = new Intent(getApplicationContext(), SeeMunicipalityActivity.class);
            intent.putExtra("municipality_id", 45);
            Animatoo.animateFade(VistualAssistantActivity.this);
            startActivity(intent);
        }

        if (botReply.equals("these are the things/places that we can see in tayug")){
            Intent intent = new Intent(getApplicationContext(), SeeMunicipalityActivity.class);
            intent.putExtra("municipality_id", 49);
            Animatoo.animateFade(VistualAssistantActivity.this);
            startActivity(intent);
        }

        if (botReply.equals("these are the things/places that we can see in umingan")){
            Intent intent = new Intent(getApplicationContext(), SeeMunicipalityActivity.class);
            intent.putExtra("municipality_id", 50);
            Animatoo.animateFade(VistualAssistantActivity.this);
            startActivity(intent);
        }

        if (botReply.equals("these are the things/places that we can see in urbiztondo")){
            Intent intent = new Intent(getApplicationContext(), SeeMunicipalityActivity.class);
            intent.putExtra("municipality_id", 67);
            Animatoo.animateFade(VistualAssistantActivity.this);
            startActivity(intent);
        }

        if (botReply.equals("these are the things/places that we can see in villasis")){
            Intent intent = new Intent(getApplicationContext(), SeeMunicipalityActivity.class);
            intent.putExtra("municipality_id", 62);
            Animatoo.animateFade(VistualAssistantActivity.this);
            startActivity(intent);
        }

        if (botReply.equals("these are the things/places that we can see in urdaneta city")){
            Intent intent = new Intent(getApplicationContext(), SeeMunicipalityActivity.class);
            intent.putExtra("municipality_id", 95);
            Animatoo.animateFade(VistualAssistantActivity.this);
            startActivity(intent);
        }

        //FESTIVAL
        //--------------------------------------------------------------------------------
        if (botReply.equals("the festival of agno")){
            int municipality_id = 31;
            initializeMunicipalityEvent(municipality_id);
        }

        if (botReply.equals("the festival of aguilar")){
            int municipality_id = 69;
            initializeMunicipalityEvent(municipality_id);
        }

        if (botReply.equals("the festival of alaminos city")){
            int municipality_id = 44;
            initializeMunicipalityEvent(municipality_id);
        }

        if (botReply.equals("the festival of alcala")){
            int municipality_id = 75;
            initializeMunicipalityEvent(municipality_id);
        }

        if (botReply.equals("the festival of anda")){
            int municipality_id = 55;
            initializeMunicipalityEvent(municipality_id);
        }

        if (botReply.equals("the festival of asingan")){
            int municipality_id = 74;
            initializeMunicipalityEvent(municipality_id);
        }

        if (botReply.equals("the festival of balungao")){
            int municipality_id = 68;
            initializeMunicipalityEvent(municipality_id);
        }

        if (botReply.equals("the festival of bani")){
            int municipality_id = 33;
            initializeMunicipalityEvent(municipality_id);
        }

        if (botReply.equals("the festival of basista")){
            int municipality_id = 57;
            initializeMunicipalityEvent(municipality_id);
        }

        if (botReply.equals("the festival of bautista")){
            int municipality_id = 70;
            initializeMunicipalityEvent(municipality_id);
        }

        if (botReply.equals("the festival of bayambang")){
            int municipality_id = 64;
            initializeMunicipalityEvent(municipality_id);
        }

        if (botReply.equals("the festival of binalonan")){
            int municipality_id = 54;
            initializeMunicipalityEvent(municipality_id);
        }

        if (botReply.equals("the festival of binmaley")){
            int municipality_id = 40;
            initializeMunicipalityEvent(municipality_id);
        }

        if (botReply.equals("the festival of bolinao")){
            int municipality_id = 32;
            initializeMunicipalityEvent(municipality_id);
        }

        if (botReply.equals("the festival of bugallon")){
            int municipality_id = 43;
            initializeMunicipalityEvent(municipality_id);
        }

        if (botReply.equals("the festival of burgos")){
            int municipality_id = 35;
            initializeMunicipalityEvent(municipality_id);
        }

        if (botReply.equals("the festival of calasiao")){
            int municipality_id = 30;
            initializeMunicipalityEvent(municipality_id);
        }

        if (botReply.equals("the festival of dagupan city")){
            int municipality_id = 47;
            initializeMunicipalityEvent(municipality_id);
        }

        if (botReply.equals("the festival of dasol")){
            int municipality_id = 36;
            initializeMunicipalityEvent(municipality_id);
        }

        if (botReply.equals("the festival of infanta")){
            int municipality_id = 37;
            initializeMunicipalityEvent(municipality_id);
        }

        if (botReply.equals("the festival of labrador")){
            int municipality_id = 38;
            initializeMunicipalityEvent(municipality_id);
        }

        if (botReply.equals("the festival of laoac")){
            int municipality_id = 60;
            initializeMunicipalityEvent(municipality_id);
        }

        if (botReply.equals("the festival of lingayen")){
            int municipality_id = 39;
            initializeMunicipalityEvent(municipality_id);
        }

        if (botReply.equals("the festival of mabini")){
            int municipality_id = 34;
            initializeMunicipalityEvent(municipality_id);
        }

        if (botReply.equals("the festival of malasiqui")){
            int municipality_id = 65;
            initializeMunicipalityEvent(municipality_id);
        }

        if (botReply.equals("the festival of manaoag")){
            int municipality_id = 66;
            initializeMunicipalityEvent(municipality_id);
        }

        if (botReply.equals("the festival of mangaldan")){
            int municipality_id = 76;
            initializeMunicipalityEvent(municipality_id);
        }

        if (botReply.equals("the festival of mangatarem")){
            int municipality_id = 46;
            initializeMunicipalityEvent(municipality_id);
        }

        if (botReply.equals("the festival of mapandan")){
            int municipality_id = 48;
            initializeMunicipalityEvent(municipality_id);
        }

        if (botReply.equals("the festival of natividad")){
            int municipality_id = 71;
            initializeMunicipalityEvent(municipality_id);
        }

        if (botReply.equals("the festival of pozorrubio")){
            int municipality_id = 72;
            initializeMunicipalityEvent(municipality_id);
        }

        if (botReply.equals("the festival of Pangasinan")){
            int municipality_id = 89;
            initializeMunicipalityEvent(municipality_id);
        }

        if (botReply.equals("the festival of rosales")){
            int municipality_id = 61;
            initializeMunicipalityEvent(municipality_id);
        }

        if (botReply.equals("the festival of san carlos city")){
            int municipality_id = 73;
            initializeMunicipalityEvent(municipality_id);
        }

        if (botReply.equals("the festival of san fabian")){
            int municipality_id = 63;
            initializeMunicipalityEvent(municipality_id);
        }

        if (botReply.equals("the festival of san jacinto")){
            int municipality_id = 52;
            initializeMunicipalityEvent(municipality_id);
        }

        if (botReply.equals("the festival of san manuel")){
            int municipality_id = 58;
            initializeMunicipalityEvent(municipality_id);
        }

        if (botReply.equals("the festival of san nicolas")){
            int municipality_id = 53;
            initializeMunicipalityEvent(municipality_id);
        }

        if (botReply.equals("the festival of san quintin")){
            int municipality_id = 59;
            initializeMunicipalityEvent(municipality_id);
        }

        if (botReply.equals("the festival of santa barbara")){
            int municipality_id = 56;
            initializeMunicipalityEvent(municipality_id);
        }

        if (botReply.equals("the festival of santa maria")){
            int municipality_id = 78;
            initializeMunicipalityEvent(municipality_id);
        }

        if (botReply.equals("the festival of santo tomas")){
            int municipality_id = 77;
            initializeMunicipalityEvent(municipality_id);
        }

        if (botReply.equals("the festival of sison")){
            int municipality_id = 51;
            initializeMunicipalityEvent(municipality_id);
        }

        if (botReply.equals("the festival of sual")){
            int municipality_id = 45;
            initializeMunicipalityEvent(municipality_id);
        }

        if (botReply.equals("the festival of tayug")){
            int municipality_id = 49;
            initializeMunicipalityEvent(municipality_id);
        }

        if (botReply.equals("the festival of umingan")){
            int municipality_id = 50;
            initializeMunicipalityEvent(municipality_id);
        }

        if (botReply.equals("the festival of urbiztondo")){
            int municipality_id = 67;
            initializeMunicipalityEvent(municipality_id);
        }

        if (botReply.equals("the festival of urdaneta city")){
            int municipality_id = 95;
            initializeMunicipalityEvent(municipality_id);
        }

        if (botReply.equals("the festival of villasis")){
            int municipality_id = 62;
            initializeMunicipalityEvent(municipality_id);
        }


        //End Things to do in urdaneta city
        //-------------------------------------------------------------------------------------
        if (botReply.equals("things to do in pangasinan")){
            thingsToDoInPangasinan();
        }

        if (botReply.equals("opening your saved and favorite destination")){
            openSavedLandmark();
        }

        if (botReply.equals("if you have a concern dont hesitate to share it with us")){
            sendFeedback();
        }

        if (botReply.equals("here is the information I found")){
            findLandmarkInformation(stringVoiceSpeechValue);
        }

        if (botReply.equals("locating destination")){
            locateLandmark(stringVoiceSpeechValue);
        }

    }




    private void sendFeedback() {
        Intent intent = new Intent(VistualAssistantActivity.this, FeedbackAndConcernActivity.class);
        Animatoo.animateFade(VistualAssistantActivity.this);
        startActivity(intent);
    }

    private void openSavedLandmark() {
        Intent intent = new Intent(VistualAssistantActivity.this, ListFavorite.class);
        Animatoo.animateFade(VistualAssistantActivity.this);
        startActivity(intent);
    }

    String eventInfo = "";
    private void initializeMunicipalityEvent(int municipality_id) {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = Constants.API_BASE_URL + "/users/get-specific-event.php?municipality_id=" + municipality_id;

        StringRequest request = new StringRequest(Request.Method.GET, url, response -> {
            try {
                JSONObject object = new JSONObject(response);
                if (object.getString("status").equals("success")) {
                    Intent intent = new Intent(getApplicationContext(), MunicipalityDetailActivity.class);
                    intent.putExtra("municipality_id", municipality_id);
                    intent.putExtra("speak_festival", "true");
                    Animatoo.animateFade(VistualAssistantActivity.this);
                    startActivity(intent);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> {
            // Handle error here, if needed
        });
        queue.add(request);

    }


    private void thingsToDoInPangasinan() {
        try {
            t1 = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    if (status == TextToSpeech.SUCCESS) {
                        int result = t1.setLanguage(Locale.ENGLISH);
                        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                            Log.e("TTS", "Language not supported.");
                        } else {
                            // Speak the text
                            t1.speak("Pangasinan is a province in the Philippines renowned for its natural beauty, rich historical sites, and vibrant culture. Visitors will find plenty of activities to enjoy, including adventures, exploring cultural heritage, savoring local cuisine and beverages, embarking on faith tours or pilgrimages, experiencing farm and nature activities, and relaxing on its beautiful islands, beaches, ridges, and reefs.", TextToSpeech.QUEUE_FLUSH, null);
                            dialog.show();
                            mainProduct.setText("Pangasinan is a province in the Philippines renowned for its natural beauty, rich historical sites, and vibrant culture. Visitors will find plenty of activities to enjoy, including adventures, exploring cultural heritage, savoring local cuisine and beverages, embarking on faith tours or pilgrimages, experiencing farm and nature activities, and relaxing on its beautiful islands, beaches, ridges, and reefs.");
                        }
                    } else {
                        Log.e("TTS", "Initialization failed.");
                    }
                }
            });

        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void getMainProduct(int municipality_id) {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = Constants.API_BASE_URL + "/users/get-municipality-detail.php?id=" + municipality_id;
        StringRequest request = new StringRequest(Request.Method.GET, url, response -> {
            try {
                JSONObject object = new JSONObject(response);
                String status = object.getString("status");
                if ("success".equals(status)) {
                    JSONObject municipality = object.getJSONObject("municipalities");
                    String main_product = municipality.getString("main_product");
                    if(main_product.isEmpty()){
                        try {
                            t1 = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
                                @Override
                                public void onInit(int status) {
                                    if (status == TextToSpeech.SUCCESS) {
                                        int result = t1.setLanguage(Locale.ENGLISH);
                                        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                                            Log.e("TTS", "Language not supported.");
                                        } else {
                                            String empty_product = "Oops!, As of the year 2023, I couldn't find any information about the main product of that municipality/city";
                                            t1.speak(empty_product, TextToSpeech.QUEUE_FLUSH, null);
                                        }
                                    } else {
                                        Log.e("TTS", "Initialization failed.");
                                    }
                                }
                            });

                        } catch (Exception e){
                            e.printStackTrace();
                        }
                    }else{
                        Intent intent = new Intent(getApplicationContext(), MunicipalityDetailActivity.class);
                        intent.putExtra("municipality_id", municipality_id);
                        intent.putExtra("speak_product", "true");
                        Animatoo.animateFade(VistualAssistantActivity.this);
                        startActivity(intent);
                    }
                } else if ("empty".equals(status)) {
                    // Handle the case when no municipalities were found
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

    private void locateLandmark(String preLandmarkName) {
        String landmarkName = removeLocateWords(preLandmarkName);
        Toast.makeText(this, "Locating : " + landmarkName, Toast.LENGTH_SHORT).show();

        RequestQueue queue = Volley.newRequestQueue(this);
        String url = Constants.API_BASE_URL + "/users/va-find-landmark-details.php?landmark_name=" + landmarkName;

        StringRequest request = new StringRequest(Request.Method.GET, url, response -> {
            try {
                JSONObject object = new JSONObject(response);
                if (object.getString("status").equals("success")) {
                    JSONArray landmarksArray = object.getJSONArray("landmarks");
                    if (landmarksArray.length() > 0) {
                        JSONObject landmark = landmarksArray.getJSONObject(0);
                        int landmark_id = landmark.getInt("landmark_id");
                        String landmark_name = landmark.getString("landmark_name");
                        String category = landmark.getString("category");
                        String address = landmark.getString("address");
                        String landmark_img_1 = landmark.getString("landmark_img_1");
                        String longitude = landmark.getString("longitude");
                        String latitude = landmark.getString("latitude");

                        Intent intent = new Intent(getApplicationContext(), MapService.class);
                        intent.putExtra("id", landmark_id);
                        intent.putExtra("landmark_name", landmark_name);
                        intent.putExtra("category", category);
                        intent.putExtra("address", address);
                        intent.putExtra("landmark_img_1", landmark_img_1);
                        intent.putExtra("long", longitude);
                        intent.putExtra("lat", latitude);
                        startActivity(intent);

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


//    private void municipalityHistory(String unfilteredMunicipalityName) {
//        String municipality_name = removeWordSearchMunicipality(unfilteredMunicipalityName);
//        Toast.makeText(this, municipality_name, Toast.LENGTH_SHORT).show();
//        RequestQueue queue = Volley.newRequestQueue(this);
//        String url = Constants.API_BASE_URL + "/users/va-municipality-history.php?municipality_name=" + municipality_name;
//
//        StringRequest request = new StringRequest(Request.Method.GET, url, response -> {
//            try {
//                JSONObject object = new JSONObject(response);
//                if (object.getString("status").equals("success")) {
//                    JSONArray landmarksArray = object.getJSONArray("municipalities");
//                    if (landmarksArray.length() > 0) {
//                        JSONObject landmark = landmarksArray.getJSONObject(0);
//                        int municipality_id = landmark.getInt("municipality_id");
//                        String municipality_lon = landmark.getString("municipality_lon");
//                        String municipality_lat = landmark.getString("municipality_lat");
//
//                        Intent intent = new Intent(getApplicationContext(), MunicipalityDetailActivity.class);
//                        intent.putExtra("municipality_id", municipality_id);
//                        intent.putExtra("long", municipality_lon);
//                        intent.putExtra("lat", municipality_lat);
//                        intent.putExtra("speak_history", "true");
//                        startActivity(intent);
//
//                    } else {
//                        // Handle the case when no landmarks were found
//                    }
//                } else if (object.getString("status").equals("empty")) {
//                    // Handle the case when no landmarks were found
//                } else {
//                    // Handle the error case
//                }
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//        }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                // Handle error response
//            }
//        });
//
//        queue.add(request);
//    }

    private String removeLocateWords(String sentence) {
        // Define the words to be removed
        String[] wordsToRemove = {"can", "locate", "me", "to", "the", "you", "our", "island", "park"};

        // Split the sentence into words
        String[] words = sentence.split(" ");
        StringBuilder modifiedSentence = new StringBuilder();

        for (String word : words) {
            // Remove the specified words
            if (!containsWord(wordsToRemove, word)) {
                // Append the word to the modified sentence if it's not one of the specified words
                modifiedSentence.append(word).append(" ");
            }
        }

        // Remove the trailing space and return the modified sentence
        return modifiedSentence.toString().trim();
    }

    private String removeWordSearchMunicipality(String sentence) {
        // Define the words to be removed
        String[] wordsToRemove = {"can", "you", "describe", "history", "what", "is", "the", "of", "city"};

        // Split the sentence into words
        String[] words = sentence.split(" ");

        // Initialize a StringBuilder to build the modified sentence
        StringBuilder modifiedSentence = new StringBuilder();

        for (String word : words) {
            // Remove the specified words
            if (!containsWord(wordsToRemove, word)) {
                // Append the word to the modified sentence if it's not one of the specified words
                modifiedSentence.append(word).append(" ");
            }
        }

        // Remove the trailing space and return the modified sentence
        return modifiedSentence.toString().trim();
    }


    private String removeWords(String sentence) {
        // Define the words to be removed
        String[] wordsToRemove = {"can", "you", "provide", "information","to", "the", "of", "our", "me", "island", "park"};

        // Split the sentence into words
        String[] words = sentence.split(" ");

        // Initialize a StringBuilder to build the modified sentence
        StringBuilder modifiedSentence = new StringBuilder();

        for (String word : words) {
            // Remove the specified words
            if (!containsWord(wordsToRemove, word)) {
                // Append the word to the modified sentence if it's not one of the specified words
                modifiedSentence.append(word).append(" ");
            }
        }

        // Remove the trailing space and return the modified sentence
        return modifiedSentence.toString().trim();
    }

    private boolean containsWord(String[] words, String targetWord) {
        for (String word : words) {
            if (word.equalsIgnoreCase(targetWord)) {
                return true;
            }
        }
        return false;
    }


    private void findLandmarkInformation(String preLandmarkName) {
        String landmarkName = removeWords(preLandmarkName);
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = Constants.API_BASE_URL + "/users/va-find-landmark-details.php?landmark_name=" + landmarkName;

        StringRequest request = new StringRequest(Request.Method.GET, url, response -> {
            try {
                JSONObject object = new JSONObject(response);
                if (object.getString("status").equals("success")) {
                    JSONArray landmarksArray = object.getJSONArray("landmarks");
                    if (landmarksArray.length() > 0) {
                        JSONObject landmark = landmarksArray.getJSONObject(0);
                        int landmark_id = landmark.getInt("landmark_id");
                        String longitude = landmark.getString("longitude");
                        String latitude = landmark.getString("latitude");

                        Intent intent = new Intent(getApplicationContext(), LandmarkDetailActivity.class);
                        intent.putExtra("id", landmark_id);
                        intent.putExtra("long", longitude);
                        intent.putExtra("lat", latitude);
                        intent.putExtra("speak_description", "true");
                        startActivity(intent);

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

    private void setUpBot() {
        try {
            InputStream stream = this.getResources().openRawResource(R.raw.credential);
            GoogleCredentials credentials = GoogleCredentials.fromStream(stream)
                    .createScoped(Lists.newArrayList("https://www.googleapis.com/auth/cloud-platform"));
            String projectId = ((ServiceAccountCredentials) credentials).getProjectId();

            SessionsSettings.Builder settingsBuilder = SessionsSettings.newBuilder();
            SessionsSettings sessionsSettings = settingsBuilder.setCredentialsProvider(
                    FixedCredentialsProvider.create(credentials)).build();
            sessionsClient = SessionsClient.create(sessionsSettings);
            sessionName = SessionName.of(projectId, uuid);

            Log.d(TAG, "projectId : " + projectId);
        } catch (Exception e) {
            Log.d(TAG, "setUpBot: " + e.getMessage());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQ_CODE: {
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    ed1.setText(result.get(0));

                    if (!result.get(0).isEmpty()) {
                        sendMessageToBot(result.get(0));
                        stringVoiceSpeechValue = result.get(0);
                    } else {
                        Toast.makeText(VistualAssistantActivity.this, "Please enter text!", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            }
        }
    }

    public void displayModal(View view) {
        va_guide_modal.show();
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
