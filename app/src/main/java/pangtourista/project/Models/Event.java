package pangtourista.project.Models;

public class Event {

    String event_title, event_date_start, event_date_end, description;
    int event_id;

    public Event(String event_title, String event_date_start, String event_date_end, String description) {
        this.event_title = event_title;
        this.event_date_start = event_date_start;
        this.event_date_end = event_date_end;
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEvent_title() {
        return event_title;
    }

    public void setEvent_title(String event_title) {
        this.event_title = event_title;
    }

    public String getEvent_date_start() {
        return event_date_start;
    }

    public void setEvent_date_start(String event_date_start) {
        this.event_date_start = event_date_start;
    }

    public String getEvent_date_end() {
        return event_date_end;
    }

    public void setEvent_date_end(String event_date_end) {
        this.event_date_end = event_date_end;
    }

    public int getEvent_id() {
        return event_id;
    }

    public void setEvent_id(int event_id) {
        this.event_id = event_id;
    }
}
