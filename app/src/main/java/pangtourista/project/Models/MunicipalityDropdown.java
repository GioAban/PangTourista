package pangtourista.project.Models;

public class MunicipalityDropdown {

    String municipality_name, municipality_id;


    public MunicipalityDropdown(String municipality_name, String municipality_id) {
        this.municipality_name = municipality_name;
        this.municipality_id = municipality_id;
    }

    public String getMunicipality_name() {
        return municipality_name;
    }

    public void setMunicipality_name(String municipality_name) {
        this.municipality_name = municipality_name;
    }

    public String getMunicipality_id() {
        return municipality_id;
    }

    public void setMunicipality_id(String municipality_id) {
        this.municipality_id = municipality_id;
    }
}
