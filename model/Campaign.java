package marketing.model;

import java.time.LocalDate;

public class Campaign {
    private String id;
    private String name;
    private String targetEvent;
    private double discountRate;
    private LocalDate startDate;
    private LocalDate endDate;

    public Campaign(String id, String name, String targetEvent, double discountRate, LocalDate startDate, LocalDate endDate) {
        this.id = id;
        this.name = name;
        this.targetEvent = targetEvent;
        this.discountRate = discountRate;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getTargetEvent() {
        return targetEvent;
    }

    public double getDiscountRate() {
        return discountRate;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }
}