package pangtourista.project.Models;

public class FiestaFestivalEvent {

    int eventId, municipalityId;
    String eventTitle, eventDateStart, eventDateEnd, municipalityName, description;

    public FiestaFestivalEvent(int eventId, int municipalityId, String eventTitle, String eventDateStart, String eventDateEnd, String municipalityName, String description) {
        this.eventId = eventId;
        this.municipalityId = municipalityId;
        this.eventTitle = eventTitle;
        this.eventDateStart = eventDateStart;
        this.eventDateEnd = eventDateEnd;
        this.municipalityName = municipalityName;
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getEventId() {
        return eventId;
    }

    public String getMunicipalityName() {
        return municipalityName;
    }

    public void setMunicipalityName(String municipalityName) {
        this.municipalityName = municipalityName;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public int getMunicipalityId() {
        return municipalityId;
    }

    public void setMunicipalityId(int municipalityId) {
        this.municipalityId = municipalityId;
    }

    public String getEventTitle() {
        return eventTitle;
    }

    public void setEventTitle(String eventTitle) {
        this.eventTitle = eventTitle;
    }

    public String getEventDateStart() {
        return eventDateStart;
    }

    public void setEventDateStart(String eventDateStart) {
        this.eventDateStart = eventDateStart;
    }

    public String getEventDateEnd() {
        return eventDateEnd;
    }

    public void setEventDateEnd(String eventDateEnd) {
        this.eventDateEnd = eventDateEnd;
    }
}
