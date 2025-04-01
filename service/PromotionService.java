package marketing.service;

import marketing.model.Campaign;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PromotionService {
    private final List<Campaign> campaigns = new ArrayList<>();

    public void createCampaign(Campaign campaign) {
        campaigns.add(campaign);
        System.out.println("Campaign created: " + campaign.getName());
    }

    public List<Campaign> getActiveCampaigns(LocalDate date) {
        List<Campaign> active = new ArrayList<>();
        for (Campaign c : campaigns) {
            if ((date.isEqual(c.getStartDate()) || date.isAfter(c.getStartDate())) &&
                    (date.isEqual(c.getEndDate()) || date.isBefore(c.getEndDate()))) {
                active.add(c);
            }
        }
        return active;
    }

    public List<Campaign> getAllCampaigns() {
        return campaigns;
    }
}