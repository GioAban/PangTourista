package pangtourista.project.Models;

public class Landmark {

    private String landmark_name, address, landmark_img_1, description_1, longitude, latitude;

    private int  landmark_id;

    public Landmark(String landmark_name, String address, String landmark_img_1, String description_1, String longitude, String latitude, int landmark_id) {
        this.landmark_name = landmark_name;
        this.address = address;
        this.landmark_img_1 = landmark_img_1;
        this.description_1 = description_1;
        this.longitude = longitude;
        this.latitude = latitude;
        this.landmark_id = landmark_id;
    }

    public String getLandmark_name() {
        return landmark_name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setLandmark_name(String landmark_name) {
        this.landmark_name = landmark_name;
    }

    public String getLandmark_img_1() {
        return landmark_img_1;
    }

    public void setLandmark_img_1(String landmark_img_1) {
        this.landmark_img_1 = landmark_img_1;
    }

    public String getDescription_1() {
        return description_1;
    }

    public void setDescription_1(String description_1) {
        this.description_1 = description_1;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public int getLandmark_id() {
        return landmark_id;
    }

    public void setLandmark_id(int landmark_id) {
        this.landmark_id = landmark_id;
    }
}
