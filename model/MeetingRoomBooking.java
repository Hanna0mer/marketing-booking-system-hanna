package marketing.model;

import java.time.LocalDateTime;

public class MeetingRoomBooking {
    private String room;
    private String requester;
    private LocalDateTime dateTime;
    private boolean approved;

    public MeetingRoomBooking(String room, String requester, LocalDateTime dateTime) {
        this.room = room;
        this.requester = requester;
        this.dateTime = dateTime;
        this.approved = false;
    }

    public String getRoom() {
        return room;
    }

    public String getRequester() {
        return requester;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }
}