package marketing.service;

import marketing.model.SingleBooking;

import java.util.ArrayList;
import java.util.List;

public class SingleBookingService {

    private final List<SingleBooking> confirmedBookings = new ArrayList<>();

    public boolean confirmSingleBooking(SingleBooking booking) {
        // Prevent double booking of the same seat at the same time
        boolean seatTaken = confirmedBookings.stream()
                .anyMatch(existing ->
                        existing.getSeatNumber().equalsIgnoreCase(booking.getSeatNumber())
                                && existing.getBookingTime().equals(booking.getBookingTime()));

        if (seatTaken) {
            return false; // Seat is already booked
        }

        confirmedBookings.add(booking);
        return true;
    }

    public List<SingleBooking> getAllConfirmedBookings() {
        return confirmedBookings;
    }
}
