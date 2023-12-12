package pangtourista.project.Models;

public class Municipality {

    private String municipality_name, municipality_desc, municipality_image, municipality_address, 	municipality_lon, municipality_lat;
    private int municipality_id;

    public Municipality(String municipality_name, String municipality_desc, String municipality_image, String municipality_address, String municipality_lon, String municipality_lat, int municipality_id) {
        this.municipality_name = municipality_name;
        this.municipality_desc = municipality_desc;
        this.municipality_image = municipality_image;
        this.municipality_address = municipality_address;
        this.municipality_lon = municipality_lon;
        this.municipality_lat = municipality_lat;
        this.municipality_id = municipality_id;
    }

    public Municipality(String municipality_name, int municipality_id) {
        this.municipality_name = municipality_name;
        this.municipality_id = municipality_id;
    }

    public String getMunicipality_name() {
        return municipality_name;
    }

    public void setMunicipality_name(String municipality_name) {
        this.municipality_name = municipality_name;
    }

    public String getMunicipality_desc() {
        return municipality_desc;
    }

    public void setMunicipality_desc(String municipality_desc) {
        this.municipality_desc = municipality_desc;
    }

    public String getMunicipality_image() {
        return municipality_image;
    }

    public void setMunicipality_image(String municipality_image) {
        this.municipality_image = municipality_image;
    }

    public String getMunicipality_address() {
        return municipality_address;
    }

    public void setMunicipality_address(String municipality_address) {
        this.municipality_address = municipality_address;
    }

    public String getMunicipality_lon() {
        return municipality_lon;
    }

    public void setMunicipality_lon(String municipality_lon) {
        this.municipality_lon = municipality_lon;
    }

    public String getMunicipality_lat() {
        return municipality_lat;
    }

    public void setMunicipality_lat(String municipality_lat) {
        this.municipality_lat = municipality_lat;
    }

    public int getMunicipality_id() {
        return municipality_id;
    }

    public void setMunicipality_id(int municipality_id) {
        this.municipality_id = municipality_id;
    }

}
