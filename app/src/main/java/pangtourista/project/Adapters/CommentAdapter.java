package pangtourista.project.Adapters;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import pangtourista.project.Models.Comment;
import pangtourista.project.Models.FiestaFestivalEvent;
import pangtourista.project.R;
import pangtourista.project.Sessions.SessionManager;
import pangtourista.project.activities.LandmarkCommentSection;
import pangtourista.project.activities.LandmarkDetailActivity;
import pangtourista.project.databinding.ItemCommentBinding;
import pangtourista.project.utils.Constants;
public class CommentAdapter extends  RecyclerView.Adapter<CommentAdapter.commentViewHolder>{
    Context context;
    ArrayList<Comment> comments;
    String session_user_id;
    public CommentAdapter(Context context, ArrayList<Comment> comments) {
        this.context = context;
        this.comments = comments;
    }
    //represents layout
    @NonNull
    @Override
    public commentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new commentViewHolder(LayoutInflater.from(context).inflate(R.layout.item_comment, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull commentViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Comment comment = comments.get(position);
        holder.binding.username.setText(comment.getUser_name());
        Glide.with(context)
                .load(comment.getUser_photo())
                .into(holder.binding.userCommentImage);
        holder.binding.commentDate.setText(comment.getCreated_at());
        holder.binding.myRatingBar.setIsIndicator(true);
        holder.binding.postComment.setText(comment.getComment());
        holder.binding.myRatingBar.setRating(Float.valueOf(comment.getUser_rating()));
        final int review_id = comment.getReview_id();
        final int user_id = comment.getUser_id();
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (Integer.valueOf(session_user_id) == user_id) {
                    showDeleteConfirmationDialog(review_id, position);
                }
                return true; // Indicate that the long click event is consumed
            }
        });
    }

    private void showDeleteConfirmationDialog(int review_id, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Confirm Delete");
        builder.setMessage("Are you sure you want to delete this review?");
        String strCommentId = String.valueOf(review_id);
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                RequestQueue queue = Volley.newRequestQueue(context);
                String url = Constants.API_BASE_URL + "/users/delete-landmark-comment.php?review_id=" + strCommentId;
                StringRequest request = new StringRequest(Request.Method.GET, url, response -> {
                    try {
                        JSONObject object = new JSONObject(response);
                        String status = object.getString("status");
                        if ("success".equals(status)) {
                            if (position >= 0 && position < comments.size()) {
                                comments.remove(position);
                                notifyItemRemoved(position);
                                notifyItemRangeChanged(position, getItemCount());
                            }
                            Toast.makeText(context, "Delete Review successfully!", Toast.LENGTH_SHORT).show();
                        } else if ("empty".equals(status)) {
                            Toast.makeText(context, "Cant review comment", Toast.LENGTH_SHORT).show();
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
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Do nothing, simply dismiss the dialog
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }
    public class commentViewHolder extends RecyclerView.ViewHolder {
        ItemCommentBinding binding;
        SessionManager sessionManager;
        public commentViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemCommentBinding.bind(itemView);
            sessionManager = new SessionManager(context);
            session_user_id = sessionManager.getUserDetail().get("USER_ID");
        }
    }
}
