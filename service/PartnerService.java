package marketing.service;

import marketing.model.EducationalPartner;

import java.util.ArrayList;
import java.util.List;

public class PartnerService {
    private final List<EducationalPartner> partners = new ArrayList<>();

    public void addPartner(EducationalPartner partner) {
        partners.add(partner);
        System.out.println("Educational partner added: " + partner.getName());
    }

    public List<EducationalPartner> getAllPartners() {
        return partners;
    }

    public EducationalPartner getPartnerByName(String name) {
        for (EducationalPartner ep : partners) {
            if (ep.getName().equalsIgnoreCase(name)) {
                return ep;
            }
        }
        return null;
    }
}