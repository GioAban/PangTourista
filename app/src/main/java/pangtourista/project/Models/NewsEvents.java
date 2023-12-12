package pangtourista.project.Models;

public class NewsEvents {

    private String ne_title, ne_description, ne_image1, ne_created_at, municipality_name, municipality_image;

    private int ne_id, municipality_id;


    public NewsEvents(String ne_title, String ne_description, String ne_image1, String ne_created_at, String municipality_name, String municipality_image, int ne_id, int municipality_id) {
        this.ne_title = ne_title;
        this.ne_description = ne_description;
        this.ne_image1 = ne_image1;
        this.ne_created_at = ne_created_at;
        this.municipality_name = municipality_name;
        this.municipality_image = municipality_image;
        this.ne_id = ne_id;
        this.municipality_id = municipality_id;
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

    public String getNe_title() {
        return ne_title;
    }

    public void setNe_title(String ne_title) {
        this.ne_title = ne_title;
    }

    public String getNe_description() {
        return ne_description;
    }

    public void setNe_description(String ne_description) {
        this.ne_description = ne_description;
    }

    public String getNe_image1() {
        return ne_image1;
    }

    public void setNe_image1(String ne_image1) {
        this.ne_image1 = ne_image1;
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
