package pangtourista.project.Models;

public class Comment {

    private String user_name, user_photo, comment, created_at;
    private int user_id, review_id;
    private String user_rating;



    public Comment(String user_name, String user_photo, String user_rating, String comment, String created_at, int user_id, int review_id) {
        this.user_name = user_name;
        this.user_photo = user_photo;
        this.user_rating = user_rating;
        this.comment = comment;
        this.created_at = created_at;
        this.user_id = user_id;
        this.review_id = review_id;
    }


    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public String getUser_name() {
        return user_name;
    }

    public String getUser_rating() {
        return user_rating;
    }

    public void setUser_rating(String user_rating) {
        this.user_rating = user_rating;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getUser_photo() {
        return user_photo;
    }

    public void setUser_photo(String user_photo) {
        this.user_photo = user_photo;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public int getReview_id() {
        return review_id;
    }

    public void setReview_id(int review_id) {
        this.review_id = review_id;
    }
}
