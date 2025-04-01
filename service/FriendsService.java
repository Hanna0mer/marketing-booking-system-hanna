package marketing.service;

import marketing.model.Friend;

import java.util.ArrayList;
import java.util.List;

public class FriendsService {
    private final List<Friend> friendsList = new ArrayList<>();

    public void addFriend(Friend friend) {
        friendsList.add(friend);
        System.out.println("Friend added: " + friend.getName());
    }

    public List<Friend> getAllFriends() {
        return friendsList;
    }

    public Friend getFriendById(String id) {
        for (Friend f : friendsList) {
            if (f.getId().equals(id)) {
                return f;
            }
        }
        return null;
    }

    public void recordPurchase(String friendId, String ticketInfo) {
        Friend friend = getFriendById(friendId);
        if (friend != null) {
            friend.addPurchase(ticketInfo);
            System.out.println("Recorded purchase for: " + friend.getName());
        }
    }
}