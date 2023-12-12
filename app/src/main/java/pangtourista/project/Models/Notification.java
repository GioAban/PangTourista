package pangtourista.project.Models;

public class Notification {

    private String ne_title, municipality_image, municipality_name, ne_created_at;
    private int ne_id, municipality_id;

    public Notification(String ne_title, String municipality_image, String municipality_name, String ne_created_at, int ne_id, int municipality_id) {
        this.ne_title = ne_title;
        this.municipality_image = municipality_image;
        this.municipality_name = municipality_name;
        this.ne_created_at = ne_created_at;
        this.ne_id = ne_id;
        this.municipality_id = municipality_id;
    }

    public String getNe_title() {
        return ne_title;
    }

    public void setNe_title(String ne_title) {
        this.ne_title = ne_title;
    }

    public String getMunicipality_image() {
        return municipality_image;
    }

    public void setMunicipality_image(String municipality_image) {
        this.municipality_image = municipality_image;
    }

    public String getMunicipality_name() {
        return municipality_name;
    }

    public void setMunicipality_name(String municipality_name) {
        this.municipality_name = municipality_name;
    }

    public String getNe_created_at() {
        return ne_created_at;
    }

    public void setNe_created_at(String ne_created_at) {
        this.ne_created_at = ne_created_at;
    }

    public int getNe_id() {
        return ne_id;
    }

    public void setNe_id(int ne_id) {
        this.ne_id = ne_id;
    }

    public int getMunicipality_id() {
        return municipality_id;
    }

    public void setMunicipality_id(int municipality_id) {
        this.municipality_id = municipality_id;
    }
}
