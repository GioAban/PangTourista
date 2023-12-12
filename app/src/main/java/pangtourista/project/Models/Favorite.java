package pangtourista.project.Models;

public class Favorite {

    int favorite_id, landmarkId;
    String landmarkName, landmarkImage, category, longitude, latitude, municipalityName;


    public Favorite(int favorite_id, String landmarkName, String category, String longitude, String latitude, int landmarkId, String landmarkImage, String municipalityName) {
        this.favorite_id = favorite_id;
        this.landmarkName = landmarkName;
        this.category = category;
        this.longitude = longitude;
        this.latitude = latitude;
        this.landmarkId = landmarkId;
        this.landmarkImage = landmarkImage;
        this.municipalityName = municipalityName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getLandmarkId() {
        return landmarkId;
    }

    public void setLandmarkId(int landmarkId) {
        this.landmarkId = landmarkId;
    }


    public int getFavorite_id() {
        return favorite_id;
    }

    public void setFavorite_id(int favorite_id) {
        this.favorite_id = favorite_id;
    }

    public String getLandmarkName() {
        return landmarkName;
    }

    public void setLandmarkName(String landmarkName) {
        this.landmarkName = landmarkName;
    }

    public String getLandmarkImage() {
        return landmarkImage;
    }

    public void setLandmarkImage(String landmarkImage) {
        this.landmarkImage = landmarkImage;
    }

    public String getMunicipalityName() {
        return municipalityName;
    }

    public void setMunicipalityName(String municipalityName) {
        this.municipalityName = municipalityName;
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
}
