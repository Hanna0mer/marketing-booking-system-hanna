package marketing.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Friend {
    private String id;
    private String name;
    private LocalDate membershipDate;
    private List<String> purchaseHistory;

    public Friend(String id, String name, LocalDate membershipDate) {
        this.id = id;
        this.name = name;
        this.membershipDate = membershipDate;
        this.purchaseHistory = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public LocalDate getMembershipDate() {
        return membershipDate;
    }

    public List<String> getPurchaseHistory() {
        return purchaseHistory;
    }

    public void addPurchase(String ticketInfo) {
        this.purchaseHistory.add(ticketInfo);
    }
}