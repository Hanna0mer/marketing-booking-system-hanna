package marketing;

import marketing.model.*;
import marketing.service.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        // Setup services
        FilmSchedulerService filmService = new FilmSchedulerService();
        GroupBookingService groupService = new GroupBookingService();
        PromotionService promoService = new PromotionService();
        FriendsService friendsService = new FriendsService();
        MeetingRoomService meetingService = new MeetingRoomService();
        PartnerService partnerService = new PartnerService();

        // Film Scheduling
        Film film = new Film("Inception", LocalDateTime.of(2025, 4, 20, 18, 30), 500, 285);
        filmService.scheduleFilm(film);

        // Group Booking
        GroupBooking group = new GroupBooking("Student Society", 15, LocalDateTime.of(2025, 4, 22, 14, 0), Arrays.asList("A1", "A2", "A3"));
        groupService.holdGroupBooking(group);
        groupService.confirmGroupBooking("Student Society");

        // Campaign
        Campaign campaign = new Campaign("SPRING25", "Spring Discount", "Inception", 0.25, LocalDate.of(2025, 4, 1), LocalDate.of(2025, 4, 30));
        promoService.createCampaign(campaign);

        // Friend
        Friend friend = new Friend("F001", "Alice Johnson", LocalDate.of(2024, 3, 10));
        friendsService.addFriend(friend);
        friendsService.recordPurchase("F001", "Inception ticket");

        // Meeting Room
        MeetingRoomBooking meeting = new MeetingRoomBooking("Conference Room A", "Marketing", LocalDateTime.of(2025, 4, 15, 10, 0));
        meetingService.bookMeetingRoom(meeting);

        // Educational Partner
        EducationalPartner partner = new EducationalPartner("Lancaster High", "contact@lhs.edu", 0.15);
        partnerService.addPartner(partner);
    }
}