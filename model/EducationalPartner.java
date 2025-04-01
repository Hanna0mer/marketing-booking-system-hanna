package marketing.model;

public class EducationalPartner {
    private String name;
    private String contact;
    private double discountRate;

    public EducationalPartner(String name, String contact, double discountRate) {
        this.name = name;
        this.contact = contact;
        this.discountRate = discountRate;
    }

    public String getName() {
        return name;
    }

    public String getContact() {
        return contact;
    }

    public double getDiscountRate() {
        return discountRate;
    }
}