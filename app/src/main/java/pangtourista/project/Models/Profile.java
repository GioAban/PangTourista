package pangtourista.project.Models;

public class Profile {

    private String user_name, user_email, user_sex;
    private int user_id;

    public Profile(String user_name, String user_email, String user_sex, int user_id) {
        this.user_name = user_name;
        this.user_email = user_email;
        this.user_sex = user_sex;
        this.user_id = user_id;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getUser_email() {
        return user_email;
    }

    public void setUser_email(String user_email) {
        this.user_email = user_email;
    }

    public String getUser_sex() {
        return user_sex;
    }

    public void setUser_sex(String user_sex) {
        this.user_sex = user_sex;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }
}
