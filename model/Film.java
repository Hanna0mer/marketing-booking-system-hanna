package marketing.model;

import java.time.LocalDateTime;

public class Film {
    private String title;
    private LocalDateTime screeningTime;
    private double cost;
    private double revenue;
    private int availableSeats;

    public Film(String title, LocalDateTime screeningTime, double cost, int availableSeats) {
        this.title = title;
        this.screeningTime = screeningTime;
        this.cost = cost;
        this.availableSeats = availableSeats;
        this.revenue = 0.0;
    }

    public String getTitle() {
        return title;
    }

    public LocalDateTime getScreeningTime() {
        return screeningTime;
    }

    public double getCost() {
        return cost;
    }

    public double getRevenue() {
        return revenue;
    }

    public int getAvailableSeats() {
        return availableSeats;
    }

    public void addRevenue(double amount) {
        this.revenue += amount;
    }

    public void setAvailableSeats(int availableSeats) {
        this.availableSeats = availableSeats;
    }
}