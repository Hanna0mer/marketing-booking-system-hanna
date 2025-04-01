package marketing.service;
import marketing.model.GroupBooking;

import java.util.ArrayList;
import java.util.List;

public class GroupBookingService {
    private final List<GroupBooking> bookings = new ArrayList<>();

    public boolean holdGroupBooking(GroupBooking booking) {
        if (booking.getGroupSize() >= 12) {
            booking.setStatus("Held");
            bookings.add(booking);
            System.out.println("Group booking held for: " + booking.getGroupName());
            return true;
        }
        return false;
    }

    public boolean confirmGroupBooking(String groupName) {
        for (GroupBooking b : bookings) {
            if (b.getGroupName().equals(groupName) && "Held".equals(b.getStatus())) {
                b.setStatus("Confirmed");
                System.out.println("Group booking confirmed for: " + groupName);
                return true;
            }
        }
        return false;
    }

    public List<GroupBooking> getAllBookings() {
        return bookings;
    }
}