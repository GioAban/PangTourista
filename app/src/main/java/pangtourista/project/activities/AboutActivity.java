package pangtourista.project.activities;

import static pangtourista.project.utils.Constants.USER_IMAGE_URL;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import pangtourista.project.R;
import pangtourista.project.databinding.ActivityAboutBinding;
import pangtourista.project.utils.Constants;

public class AboutActivity extends AppCompatActivity {
    ActivityAboutBinding binding;
    String facebook_link = "";
    String instagram_link = "";
    String youtube_link = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        binding = ActivityAboutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        RequestQueue queue = Volley.newRequestQueue(this);
        String url = Constants.API_BASE_URL+"/users/get-organization-about-information.php";
        StringRequest request = new StringRequest(Request.Method.GET, url, response -> {
            try {
                JSONObject object = new JSONObject(response);
                if (object.getString("status").equals("success")) {
                    JSONArray profileArray = object.getJSONArray("about");
                    if (profileArray.length() > 0) {
                        JSONObject about = profileArray.getJSONObject(0);
                        String org_name = about.getString("org_name");
                        String org_description = about.getString("org_description");
                        String org_background = about.getString("org_background");
                        String org_vision = about.getString("org_vision");
                        String org_mission = about.getString("org_mission");

                         facebook_link = about.getString("facebook_link");
                         instagram_link = about.getString("instagram_link");
                         youtube_link = about.getString("youtube_link");

                        Spanned idNameSpannedText = Html.fromHtml(org_name, Html.FROM_HTML_MODE_LEGACY);
                        Spanned idDescriptionSpannedText = Html.fromHtml(org_description, Html.FROM_HTML_MODE_LEGACY);
                        Spanned idBackgroundSpannedText = Html.fromHtml(org_background, Html.FROM_HTML_MODE_LEGACY);
                        Spanned idVisionSpannedText = Html.fromHtml(org_vision, Html.FROM_HTML_MODE_LEGACY);
                        Spanned idMissionSpannedText = Html.fromHtml(org_mission, Html.FROM_HTML_MODE_LEGACY);

                        binding.idName.setText(idNameSpannedText);
                        binding.idDescription.setText(idDescriptionSpannedText);
                        binding.idBackground.setText(idBackgroundSpannedText);
                        binding.idVision.setText(idVisionSpannedText);
                        binding.idMission.setText(idMissionSpannedText);

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

    public void go_to_faceBook(View view) {
        String facebookUrl = facebook_link;
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(facebookUrl));

            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            } else {
                // If the Facebook app is not installed, open in a web browser
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse(facebookUrl));
                startActivity(intent);
            }
        } catch (Exception e) {
            // Handle exceptions, such as malformed URL or no activity found to handle the Intent
            Toast.makeText(this, "Unable to open Facebook.", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
    public void go_to_instagram(View view) {
        String instagramUrl = instagram_link;
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(instagramUrl));

            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            } else {
                // If the Instagram app is not installed, open in a web browser
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse(instagramUrl));
                startActivity(intent);
            }
        } catch (Exception e) {
            // Handle exceptions, such as malformed URL or no activity found to handle the Intent
            Toast.makeText(this, "Unable to open Instagram.", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    public void go_to_youTube(View view) {
        String youTubeUrl = youtube_link;
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(youTubeUrl));

            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            } else {
                // If the YouTube app is not installed, open in a web browser
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse(youTubeUrl));
                startActivity(intent);
            }
        } catch (Exception e) {
            // Handle exceptions, such as malformed URL or no activity found to handle the Intent
            Toast.makeText(this, "Unable to open YouTube.", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
}
