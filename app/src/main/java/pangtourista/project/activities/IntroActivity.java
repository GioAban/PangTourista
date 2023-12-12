package pangtourista.project.activities;

import androidx.annotation.FloatRange;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.View;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import io.github.dreierf.materialintroscreen.MaterialIntroActivity;
import io.github.dreierf.materialintroscreen.SlideFragmentBuilder;
import io.github.dreierf.materialintroscreen.animations.IViewTranslation;
import pangtourista.project.R;
import pangtourista.project.Sessions.SessionManager;
import pangtourista.project.utils.Constants;

public class IntroActivity extends MaterialIntroActivity {
    SessionManager sessionManager;
    String loggedInUserId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionManager = new SessionManager(this);
        loggedInUserId = sessionManager.getUserDetail().get("USER_ID");

        addSlide(new SlideFragmentBuilder()
                .title("PangTourista Home Page\n")
                .description("This is the application homepage where we can find clickable icons for discovering destinations, news & events, festivals, and nearby places. Here, we can also explore things to do, such as adventure, cultural heritage, pilgrimage, etc., as well as the municipalities in Pangasinan")
                .image(R.drawable.dashboard)
                .buttonsColor(R.color.colorGrey)
                .backgroundColor(R.color.dashboard_color)
                .build());

        addSlide(new SlideFragmentBuilder()
                .title("Other Feature\n")
                .description("The icons here are clickable. Simply click on 'Find Destination' to search for landmarks, spots, and attractions. Press the 'News and Events' icon to view news and events in Pangasinan. For the 'Fiestas and Festivals' icon, you will find a list of fiestas and festivals. Lastly, click on 'Nearby Places' to locate nearby hotels, restaurants, police stations, repair shops, and more.")
                .image(R.drawable.other_feature)
                .buttonsColor(R.color.colorGrey)
                .backgroundColor(R.color.dashboard_color)
                .build());

        addSlide(new SlideFragmentBuilder()
                .title("Search Municipality/City\n")
                .description("To find a municipality, type its name in the search bar and tap your phone's search key. For a comprehensive list, scroll through the cards. Each card displays image, name, and address of municipalities and cities. Click to access detailed information, including maps and navigation, as well as a button to view selected municipality or city landmarks.")
                .image(R.drawable.municipalities_cities)
                .buttonsColor(R.color.colorGrey)
                .backgroundColor(R.color.dashboard_color)
                .build());

        addSlide(new SlideFragmentBuilder()
                .title("Things to do\n")
                .description("Scroll the card to the left to reveal additional activities such as adventures, cultural heritage sites, beaches, islands, pilgrimages, products, etc. Clicking on these will display a card list for the selected category of things to do.")
                .image(R.drawable.things_to_do)
                .buttonsColor(R.color.colorGrey)
                .backgroundColor(R.color.dashboard_color)
                .build());

        addSlide(new SlideFragmentBuilder()
                .title("View landmark details\n")
                .description("Here are the details of the selected landmark: name, address, rates, status, and description or history. You'll find clickable icons to watch a video of the landmark, save or bookmark it to your account, and an additional info icon to view more details about the landmark. You can also review the landmark by clicking the \"View Reviews\" hyperlink to see tourist reviews.")
                .image(R.drawable.view_landmark_detail)
                .buttonsColor(R.color.colorGrey)
                .backgroundColor(R.color.dashboard_color)
                .build());

        addSlide(new SlideFragmentBuilder()
                .title("Maps and Navigation\n")
                .description("When you click the navigation button, you'll access your exact location's route to the destination. The application automatically retrieves this. There's a silent icon available in case you prefer the application not to provide turn-by-turn vocal guidance. Additionally, clicking \"Re-Center\" will refocus the map on your current location.")
                .image(R.drawable.maps_and_navigation)
                .buttonsColor(R.color.colorGrey)
                .backgroundColor(R.color.dashboard_color)
                .build());

        addSlide(new SlideFragmentBuilder()
                .title("Virtual Assistant\n")
                .description("Look for a quiet place and make sure you have an internet connection. Tap the bot or virtual assistant and start speaking. You can inquire about the history of the municipality or city, notable sights, nearby places, local products, festivals, the latest news, and how to find destinations. It's crucial to know what questions to ask the virtual assistant. The icon below is the virtual assistant guide.")
                .image(R.drawable.virtual_assistant)
                .buttonsColor(R.color.colorGrey)
                .backgroundColor(R.color.dashboard_color)
                .build());

        addSlide(new SlideFragmentBuilder()
                .title("Virtual Assistant Guide\n")
                .description("This is the virtual assistant guide. Here, you will find the questions that we can only ask the virtual assistant. If the virtual assistant cannot recognize your question, it will repeatedly display on your screen. For example, if you say 'things to do in Pangasinan,' the key phrase is 'things to do,' and the virtual assistant will provide you with information related to your query. When locating a destination, always use the word 'locate.' For instance, if you want to find a destination, you can say, 'Locate me to [mention the name of the destination].")
                .image(R.drawable.virtual_assistant_guide)
                .buttonsColor(R.color.colorGrey)
                .backgroundColor(R.color.dashboard_color)
                .build());

        addSlide(new SlideFragmentBuilder()
                .title("Feedback & Concerns")
                .image(R.drawable.feedback_concern_ui)
                .description("When submitting feedback and concerns, you can simply type your message in the input text. You also have the option to click on the image icon to attach a photo. Please refrain from using inappropriate language in your submissions.")
                .buttonsColor(R.color.colorGrey)
                .backgroundColor(R.color.dashboard_color)
                .build());

                getSkipButtonTranslationWrapper()
                .setExitTranslation(new IViewTranslation() {
                    @Override
                    public void translate(View view, float percentage) {

                        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
                        String url = Constants.API_BASE_URL + "/users/update-first-user.php";
                        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                                response -> {
                                    if (response.equals("success")) {

                                    } else {
                                        Toast.makeText(IntroActivity.this, response, Toast.LENGTH_LONG).show();
                                    }
                                }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                error.printStackTrace();
                            }
                        }) {
                            protected Map<String, String> getParams() {
                                Map<String, String> paramV = new HashMap<>();
                                paramV.put("user_id", loggedInUserId);
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
                });
    }
}