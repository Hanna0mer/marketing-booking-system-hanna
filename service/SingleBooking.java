package marketing.model;

import java.time.LocalDateTime;

public class SingleBooking {
    private String customerName;
    private LocalDateTime bookingTime;
    private String seatNumber;
    private double price;

    public SingleBooking(String customerName, LocalDateTime bookingTime, String seatNumber) {
        this.customerName = customerName;
        this.bookingTime = bookingTime;
        this.seatNumber = seatNumber;
    }

    public String getCustomerName() { return customerName; }
    public LocalDateTime getBookingTime() { return bookingTime; }
    public String getSeatNumber() { return seatNumber; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
}