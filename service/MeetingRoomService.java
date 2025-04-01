package marketing.service;

import marketing.model.MeetingRoomBooking;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MeetingRoomService {
    private final List<MeetingRoomBooking> bookings = new ArrayList<>();

    public boolean bookMeetingRoom(MeetingRoomBooking booking) {
        if (isAvailable(booking.getRoom(), booking.getDateTime())) {
            bookings.add(booking);
            System.out.println("Meeting room booked: " + booking.getRoom() + " for " + booking.getRequester());
            return true;
        }
        return false;
    }

    public boolean isAvailable(String room, LocalDateTime dateTime) {
        for (MeetingRoomBooking b : bookings) {
            if (b.getRoom().equals(room) && b.getDateTime().equals(dateTime)) {
                return false;
            }
        }
        return true;
    }

    public List<MeetingRoomBooking> getAllBookings() {
        return bookings;
    }
}