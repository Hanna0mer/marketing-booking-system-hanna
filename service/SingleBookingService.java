package marketing.service;

import marketing.db.MySQLDatabaseHelper;
import marketing.model.SingleBooking;
import java.sql.Timestamp;

public class SingleBookingService {
    public boolean confirmSingleBooking(SingleBooking booking) {
        String customerName = booking.getCustomerName();
        Timestamp bookingTime = Timestamp.valueOf(booking.getBookingTime()); // Convert LocalDateTime to Timestamp
        String seatNumber = extractSeatNumber(booking.getSeatNumber());
        double price = booking.getPrice();

        MySQLDatabaseHelper.saveSingleBooking(customerName, bookingTime, seatNumber, price);
        return true; // Success assumed if no exception
    }

    private String extractSeatNumber(String fullSeat) {
        return fullSeat.split(" ")[0]; // e.g., "A1" from "A1 (Main Hall - Stalls)"
    }
}