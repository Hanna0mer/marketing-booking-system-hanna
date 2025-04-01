package marketing.model;

import java.time.LocalDateTime;

public class SingleBooking {
    private String customerName;
    private LocalDateTime bookingTime;
    private String seatNumber;

    public SingleBooking(String customerName, LocalDateTime bookingTime, String seatNumber) {
        this.customerName = customerName;
        this.bookingTime = bookingTime;
        this.seatNumber = seatNumber;
    }

    public String getCustomerName() {
        return customerName;
    }

    public LocalDateTime getBookingTime() {
        return bookingTime;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public void setBookingTime(LocalDateTime bookingTime) {
        this.bookingTime = bookingTime;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }
}
