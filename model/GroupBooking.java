package marketing.model;

import java.time.LocalDateTime;
import java.util.List;

public class GroupBooking {
    private String groupName;
    private int groupSize;
    private LocalDateTime bookingTime;
    private List<String> heldRows;
    private String status;

    public GroupBooking(String groupName, int groupSize, LocalDateTime bookingTime, List<String> heldRows) {
        this.groupName = groupName;
        this.groupSize = groupSize;
        this.bookingTime = bookingTime;
        this.heldRows = heldRows;
        this.status = "Pending";
    }

    public String getGroupName() {
        return groupName;
    }

    public int getGroupSize() {
        return groupSize;
    }

    public LocalDateTime getBookingTime() {
        return bookingTime;
    }

    public List<String> getHeldRows() {
        return heldRows;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}