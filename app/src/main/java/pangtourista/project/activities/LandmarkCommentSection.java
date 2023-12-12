package pangtourista.project.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pangtourista.project.Adapters.CommentAdapter;
import pangtourista.project.Models.Comment;
import pangtourista.project.R;
import pangtourista.project.Sessions.SessionManager;
import pangtourista.project.databinding.ActivityLandmarkCommentSectionBinding;
import pangtourista.project.utils.Constants;
import android.os.Bundle;
import android.os.Handler;

public class LandmarkCommentSection extends AppCompatActivity {
    SessionManager sessionManager;
    String loggedInUserId;
    CommentAdapter commentAdapter;
    ArrayList<Comment> arrComments;
    ActivityLandmarkCommentSectionBinding binding;
    ImageButton send_comment_button;
    ImageView feedback_image;
    TextInputEditText input_comment;
    RecyclerView commentList;
    TextView commentVisibility;
    private Toolbar toolbar;
    TextView rateCount;
    RatingBar ratingBar;
    float rateValue;
    LinearLayout inputReview;
    private SwipeRefreshLayout swipeRefreshLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        binding = ActivityLandmarkCommentSectionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        sessionManager = new SessionManager(this);
        loggedInUserId = sessionManager.getUserDetail().get("USER_ID");

        toolbar = findViewById(R.id.comment);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Review & Ratings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        int whiteColor = ContextCompat.getColor(this, android.R.color.white);
        toolbar.setTitleTextColor(whiteColor);
        setNavigationIconColor(toolbar, whiteColor);

        int landmark_id = getIntent().getIntExtra("id",0);
        getUserComment(landmark_id);
        check_user_already_review(landmark_id, loggedInUserId);


        arrComments = new ArrayList<>();
        commentAdapter = new CommentAdapter(this, arrComments);

        GridLayoutManager layoutManager = new GridLayoutManager(this, 1);
        binding.commentList.setLayoutManager(layoutManager);
        binding.commentList.setAdapter(commentAdapter);

        send_comment_button = findViewById(R.id.send_comment_button);
        input_comment = findViewById(R.id.input_comment);
        feedback_image = findViewById(R.id.feedback_image);
        commentList = findViewById(R.id.commentList);
        commentVisibility = findViewById(R.id.commentVisibility);

        rateCount = findViewById(R.id.rateCount);
        ratingBar = findViewById(R.id.myRatingBar);
        inputReview = findViewById(R.id.inputReview);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);


        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                rateValue = ratingBar.getRating();
                if (rateValue<=1 && rateValue>0)
                    rateCount.setText("Poor " + rateValue + "/5");
                else if (rateValue<=2 && rateValue>1)
                    rateCount.setText("Below Par " + rateValue + "/5");
                else if (rateValue<=3 && rateValue>1)
                    rateCount.setText("Average " + rateValue + "/5");
                else if (rateValue<=4 && rateValue>1)
                    rateCount.setText("Good " + rateValue + "/5");
                else if (rateValue<=5 && rateValue>1)
                    rateCount.setText("Excellent " + rateValue + "/5");
            }
        });


        send_comment_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmSubmitReview(landmark_id);
            }
        });

        // Set the refreshing listener
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Implement the refresh action here
                check_user_already_review(landmark_id, loggedInUserId);
                // Simulate a delay
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // Stop the refreshing animation

                        swipeRefreshLayout.setRefreshing(false);
                    }
                }, 2000); // 2000 milliseconds (2 seconds) delay, adjust as needed
            }
        });
    }

    private void confirmSubmitReview(int landmark_id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this); // Use 'this' instead of 'getApplicationContext()'
        builder.setTitle("Confirm Submit Review");
        builder.setMessage("Are you sure you want to Submit review?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                submitComment(landmark_id, loggedInUserId, String.valueOf(rateValue));
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Do nothing, simply dismiss the dialog
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void check_user_already_review(int landmark_id, String loggedInUserId) {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = Constants.API_BASE_URL + "/users/is-user-reviewed.php?landmark_id=" + landmark_id + "&user_id=" + loggedInUserId;
        StringRequest request = new StringRequest(Request.Method.GET, url, response -> {
            try {
                JSONObject object = new JSONObject(response);
                String status = object.getString("status");
                if ("success".equals(status)) {
                    JSONArray is_user_reviewed = object.getJSONArray("is_user_reviewed");
                    if (is_user_reviewed.length() > 0) {
                        inputReview.setVisibility(View.GONE);
                    } else {
                        inputReview.setVisibility(View.VISIBLE);
                    }
                } else if ("empty".equals(status)) {
                    inputReview.setVisibility(View.VISIBLE);
                } else {
                    // Handle other error cases
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> {
            // Handle error response (e.g., network error)
            error.printStackTrace();
        });
        queue.add(request);
    }

    private void submitComment(int landmark_id, String user_id, String rating) {
        String comment = input_comment.getText().toString().trim();

        if (comment.isEmpty()) {
            Toast.makeText(this, "Say something!", Toast.LENGTH_SHORT).show();
        } else {
            if (containsBadWord(comment)) {

            } else {

                RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
                String url = Constants.API_BASE_URL + "/users/submit-comment.php";
                StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                        response -> {
                            if (response.equals("success")) {
                                input_comment.setText("");
                                arrComments.clear();
                                getUserComment(landmark_id);
                                inputReview.setVisibility(View.GONE);
                                sendNotificationToAdmin(user_id,landmark_id,rating,comment);
                                Toast.makeText(LandmarkCommentSection.this, "Review posted successfully!", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(LandmarkCommentSection.this, "Something went wrong!", Toast.LENGTH_LONG).show();
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                }) {
                    protected Map<String, String> getParams() {
                        Map<String, String> paramV = new HashMap<>();
                        paramV.put("landmark_id", String.valueOf(landmark_id));
                        paramV.put("user_id", user_id);
                        paramV.put("rating", rating);
                        paramV.put("comment", comment);
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
                // API End
            }
        }
    }

    private void sendNotificationToAdmin(String user_id, int landmark_id, String rating, String comment) {
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String url = Constants.API_BASE_URL + "/users/submit-comment-notification.php";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    if (response.equals("success")) {
//                        Toast.makeText(LandmarkCommentSection.this, "Review posted successfully!", Toast.LENGTH_SHORT).show();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        }) {
            protected Map<String, String> getParams() {
                Map<String, String> paramV = new HashMap<>();
                paramV.put("user_id", user_id);
                paramV.put("landmark_id", String.valueOf(landmark_id));
                paramV.put("rating", rating);
                paramV.put("comment", comment);
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

    private boolean containsBadWord(String text) {
        List<String> badWords = Arrays.asList("putangena", "kapangit", "shet", "Ukininana",
                "Oketnana", "gago", "gagi", "tanga", "puta", "panget", "tangina", "buwisit", "inutil", "fuck", "bobo", "iyot","pangit", "ukininana", "okininana", "tarantado", "shit",
                "putang ina", "putangina", "tangina","bobo ka", "ulol", "gunggong", "hayop", "gaga", "leche", "uto-uto", "tangang", "stupid", "idiot", "moron",
                "asshole", "bastard", "dumbass", "fucker", "motherfucker", "shitty", "wanker", "jerk", "c*nt", "duckhead", "pr*ck"
        );

        // Split the input text into words
        String[] words = text.split("\\s+");

        // Check if any of the words are in the list of bad words
        for (String word : words) {
            if (badWords.contains(word.toLowerCase())) {
                Toast.makeText(this, "The comment contains inappropriate language!", Toast.LENGTH_LONG).show();
                return true;
            }
        }

        return false;
    }

    void getUserComment(int landmark_id) {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = Constants.API_BASE_URL+"/users/get-comment-list.php?landmark_id=" + landmark_id;
        StringRequest request = new StringRequest(Request.Method.GET, url, response -> {
            try {
                JSONObject object = new JSONObject(response);
                if(object.getString("status").equals("success")){
                    JSONArray co = object.getJSONArray("comments");
                    for(int i =0; i< co.length(); i++) {
                        JSONObject childObj = co.getJSONObject(i);
                        Comment comment = new Comment(
                                childObj.getString("user_name"),
                                Constants.USER_IMAGE_URL + childObj.getString("user_photo"),
                                childObj.getString("user_rating"),
                                childObj.getString("comment"),
                                childObj.getString("created_at"),
                                childObj.getInt("user_id"),
                                childObj.getInt("review_id")
                        );
                        arrComments.add(comment);
                    }
                    if (co.length()>0){
                        commentList.setVisibility(View.VISIBLE);
                        commentVisibility.setVisibility(View.GONE);
                    }
                    commentAdapter.notifyDataSetChanged();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> { });

        queue.add(request);
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