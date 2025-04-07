// BookingAppSwing.java - Final Full Version with Accurate Seating Layouts
package marketing;

import marketing.model.GroupBooking;
import marketing.model.SingleBooking;
import marketing.service.GroupBookingService;
import marketing.service.SingleBookingService;

import marketing.db.MySQLDatabaseHelper;
import java.sql.Timestamp;


import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.Map;


public class BookingAppSwing extends JFrame {
    private final GroupBookingService groupService = new GroupBookingService();
    private final SingleBookingService singleService = new SingleBookingService();
    private final List<GroupBooking> confirmedGroupBookings = new ArrayList<>();
    private final List<SingleBooking> confirmedSingleBookings = new ArrayList<>();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final JLabel groupStatus = new JLabel();

    // Meeting room data (name -> [maxCapacity, hourlyRate, halfDay, fullDay])
    private final Map<String, int[]> meetingRoomData = Map.of(
            "The Green Room", new int[]{20, 25, 75, 130},
            "Brontë Boardroom", new int[]{40, 40, 120, 200},
            "Dickens Den", new int[]{25, 30, 90, 150},
            "Poe Parlor", new int[]{30, 35, 100, 170},
            "Globe Room", new int[]{50, 50, 150, 250},
            "Chekhov Chamber", new int[]{35, 38, 110, 180}
    );


    private DefaultTableModel groupModel;
    private DefaultTableModel singleModel;

    public BookingAppSwing() {
        setTitle("Marketing Booking System");
        setSize(900, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.add("Single Booking", createSingleBookingPanel());
        tabbedPane.add("Group Booking", createGroupBookingPanel());
        tabbedPane.add("Confirmed Bookings", createConfirmedPanel());

        add(tabbedPane);

        JLabel clockLabel = new JLabel();
        clockLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        clockLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 10));
        add(clockLabel, BorderLayout.SOUTH);

// Live clock thread
        new Timer(1000, e -> {
            clockLabel.setText("Time now: " + java.time.LocalTime.now().withNano(0).toString());
        }).start();

    }

    private JPanel createSingleBookingPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(0, 2, 10, 10));
        JTextField customerName = new JTextField();
        JCheckBox wheelchairBox = new JCheckBox("Wheelchair Accessible");
        JLabel status = new JLabel();

        JComboBox<String> roomBox = new JComboBox<>(new String[]{"Main Hall - Stalls", "Main Hall - Balcony", "Small Hall", "Rehearsal Room"});
        JComboBox<String> timeBox = createTimeDropdown();
        JSpinner dateSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor editor = new JSpinner.DateEditor(dateSpinner, "dd/MM/yyyy");
        dateSpinner.setEditor(editor);

        JPanel seatPanel = new JPanel(new GridLayout(10, 20));
        ButtonGroup seatGroup = new ButtonGroup();
        List<JToggleButton> seatButtons = new ArrayList<>();

        Runnable updateSeatGrid = () -> {
            seatPanel.removeAll();
            seatButtons.clear();
            seatGroup.clearSelection();

            String room = (String) roomBox.getSelectedItem();
            if (room == null) return;

            List<String> allSeats = room.contains("Main Hall") ? generateMainHallSeats() :
                    room.contains("Small Hall") ? generateSmallHallSeats() :
                            new ArrayList<>();

            LocalDate date = ((java.util.Date) dateSpinner.getValue()).toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
            String timeStr = (String) timeBox.getSelectedItem();
            if (date == null || timeStr == null) return;
            LocalDateTime bookingTime = LocalDateTime.of(date, LocalTime.parse(timeStr));

            for (String seat : allSeats) {
                JToggleButton btn = new JToggleButton(seat);
                if (isSeatBooked(seat, bookingTime)) {
                    btn.setEnabled(false);
                }
                seatPanel.add(btn);
                seatButtons.add(btn);
                seatGroup.add(btn);
            }

            seatPanel.revalidate();
            seatPanel.repaint();
        };

        roomBox.addActionListener(e -> updateSeatGrid.run());
        timeBox.addActionListener(e -> updateSeatGrid.run());
        dateSpinner.addChangeListener(e -> updateSeatGrid.run());

        JButton confirmBtn = new JButton("Confirm Single Booking");
        confirmBtn.addActionListener((ActionEvent e) -> {
            try {
                LocalDate date = ((java.util.Date) dateSpinner.getValue()).toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
                String timeStr = (String) timeBox.getSelectedItem();
                if (date == null || timeStr == null) {
                    status.setText("⚠️ Select date and time."); return;
                }
                LocalDateTime bookingTime = LocalDateTime.of(date, LocalTime.parse(timeStr));
                String name = customerName.getText().trim();
                String room = (String) roomBox.getSelectedItem();
                boolean wheelchair = wheelchairBox.isSelected();

                if (room == null || room.isEmpty()) { status.setText("⚠️ Select a room."); return; }

                String selectedSeat = seatButtons.stream()
                        .filter(AbstractButton::isSelected)
                        .map(AbstractButton::getText)
                        .findFirst().orElse(null);

                if (selectedSeat == null) {
                    status.setText("❌ Please select a seat."); return;
                }

                if (selectedSeat.matches("R.*")) {
                    status.setText("❌ Restricted view seat."); return;
                }

                if (wheelchair && !(selectedSeat.startsWith("A") || selectedSeat.startsWith("L") || selectedSeat.endsWith("1") || selectedSeat.endsWith("10"))) {
                    status.setText("⚠️ Accessible seats only on Row A/L or edges."); return;
                }

                double price = calculateSinglePrice(room, bookingTime);
                SingleBooking booking = new SingleBooking(name, bookingTime, selectedSeat + " (" + room + ")");
                booking.setPrice(price);

                System.out.println("Attempting to confirm: " + name + ", " + bookingTime + ", " + selectedSeat + ", " + price);
                boolean isSeatAvailable = !isSeatBooked(selectedSeat, bookingTime);
                System.out.println("Is seat available? " + isSeatAvailable);
                if (isSeatAvailable && singleService.confirmSingleBooking(booking)) {
                    confirmedSingleBookings.add(booking);
                    singleModel.addRow(new Object[]{
                            booking.getCustomerName(),
                            booking.getBookingTime().format(formatter),
                            booking.getSeatNumber(),
                            "£" + String.format("%.2f", booking.getPrice())
                    });
                    status.setText("✅ Booking confirmed. Price: £" + String.format("%.2f", price));
                    System.out.println("Seat numeber: " + booking.getSeatNumber());
                    //MySQLDatabaseHelper.saveSingleBooking(booking.getCustomerName(), Timestamp.valueOf(bookingTime), booking.getSeatNumber(), booking.getPrice());
                    System.out.println("Booking confirmed in UI");
                } else {
                    status.setText("❌ Seat taken.");
                    System.out.println("Booking rejected: seat taken or service failed");
                }
            } catch (Exception ex) {
                status.setText("⚠️ Invalid input.");
                System.out.println("Exception in confirm: " + ex.getMessage());
            }
        });

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(ev -> {
            customerName.setText("");
            wheelchairBox.setSelected(false);
            roomBox.setSelectedIndex(-1);
            timeBox.setSelectedIndex(-1);
            dateSpinner.setValue(new java.util.Date());
            seatButtons.forEach(b -> b.setSelected(false));
            seatPanel.removeAll();
            seatPanel.revalidate();
            seatPanel.repaint();
            status.setText("");
        });

        form.add(new JLabel("Customer Name:")); form.add(customerName);
        form.add(new JLabel("Date:")); form.add(dateSpinner);
        form.add(new JLabel("Time:")); form.add(timeBox);
        form.add(new JLabel("Room:")); form.add(roomBox);
        form.add(wheelchairBox); form.add(new JLabel());
        form.add(confirmBtn); form.add(refreshBtn);
        form.add(status);

        panel.add(form, BorderLayout.NORTH);
        panel.add(new JScrollPane(seatPanel), BorderLayout.CENTER);
        return panel;
    }


    private JPanel createGroupBookingPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setLayout(new GridLayout(0, 2, 10, 10));

        JPanel form = new JPanel(new GridLayout(0, 2, 10, 10));
        JTextField groupName = new JTextField();
        JTextField groupSize = new JTextField();
        JSpinner dateSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor editor = new JSpinner.DateEditor(dateSpinner, "dd/MM/yyyy");
        dateSpinner.setEditor(editor);
        JComboBox<String> timeBox = createTimeDropdown();
        JComboBox<String> roomBox = new JComboBox<>();
        final JLabel sessionLabel = new JLabel("Session Type:");
        final JComboBox<String> sessionBox = new JComboBox<>(new String[]{"1 Hour", "Morning", "Afternoon", "All Day"});
        sessionLabel.setVisible(false);
        sessionBox.setVisible(false);

        roomBox.addActionListener(e -> {
            String selected = (String) roomBox.getSelectedItem();
            boolean isMeetingOrRehearsal = selected != null &&
                    (meetingRoomData.containsKey(selected) || selected.equals("Rehearsal Room"));

            sessionBox.setVisible(isMeetingOrRehearsal);
            sessionLabel.setVisible(isMeetingOrRehearsal);
        });

        JLabel priceLabel = new JLabel("Total Price: £0.00");

        JPanel seatPanel = new JPanel(new GridLayout(10, 20));
        List<JToggleButton> seatButtons = new ArrayList<>();

        // Regenerate seat panel based on room
        Runnable updateSeatGrid = () -> {
            seatPanel.removeAll();
            seatButtons.clear();
            String room = (String) roomBox.getSelectedItem();
            if (room == null) return;

            List<String> allSeats = room.contains("Main Hall") ? generateMainHallSeats() :
                    room.contains("Small Hall") ? generateSmallHallSeats() :
                            new ArrayList<>();

            LocalDate date = ((java.util.Date) dateSpinner.getValue()).toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
            String timeStr = (String) timeBox.getSelectedItem();
            if (date == null || timeStr == null) return;
            LocalDateTime bookingTime = LocalDateTime.of(date, LocalTime.parse(timeStr));

            for (String seat : allSeats) {
                JToggleButton btn = new JToggleButton(seat);
                if (isSeatBooked(seat, bookingTime)) {
                    btn.setEnabled(false);
                }
                seatPanel.add(btn);
                seatButtons.add(btn);
            }
            seatPanel.revalidate();
            seatPanel.repaint();
        };

        groupSize.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { updateRooms(); }
            public void removeUpdate(DocumentEvent e) { updateRooms(); }
            public void changedUpdate(DocumentEvent e) { updateRooms(); }
            private void updateRooms() {
                try {
                    int size = Integer.parseInt(groupSize.getText().trim());
                    roomBox.removeAllItems();
                    if (size <= 30) {
                        roomBox.addItem("Rehearsal Room");

                        // Add actual named meeting rooms from meetingRoomData
                        for (String name : meetingRoomData.keySet()) {
                            roomBox.addItem(name);
                        }
                    }

                    if (size <= 95) roomBox.addItem("Small Hall");
                    if (size <= 240) {
                        roomBox.addItem("Main Hall - Stalls");
                        roomBox.addItem("Main Hall - Balcony");
                    }
                } catch (Exception ignored) {
                    roomBox.removeAllItems();
                }
            }
        });

        roomBox.addActionListener(e -> {
            String selected = (String) roomBox.getSelectedItem();
            boolean isMeetingOrRehearsal = selected != null &&
                    (meetingRoomData.containsKey(selected) || selected.equals("Rehearsal Room"));

            sessionBox.setVisible(isMeetingOrRehearsal);
            sessionLabel.setVisible(isMeetingOrRehearsal);
            if (isMeetingOrRehearsal) sessionBox.setSelectedIndex(0);
        });

        timeBox.addActionListener(e -> updateSeatGrid.run());
        dateSpinner.addChangeListener(e -> updateSeatGrid.run());

        JButton holdBtn = new JButton("Hold Group Booking");
        JButton confirmBtn = new JButton("Confirm Group Booking");

        holdBtn.addActionListener((ActionEvent e) -> {
            try {
                int size = Integer.parseInt(groupSize.getText().trim());
                LocalDate date = ((java.util.Date) dateSpinner.getValue()).toInstant()
                        .atZone(java.time.ZoneId.systemDefault()).toLocalDate();
                String timeStr = (String) timeBox.getSelectedItem();
                String room = (String) roomBox.getSelectedItem();
                String session = (String) sessionBox.getSelectedItem(); // NEW: session type
                boolean isMeetingRoom = meetingRoomData.containsKey(room);
                boolean isRehearsal = room.equals("Rehearsal Room");

                if (size < 12 || date == null || timeStr == null || room == null || room.isEmpty()) {
                    groupStatus.setText("⚠️ Fill all fields correctly.");
                    return;
                }

                LocalDateTime bookingTime = LocalDateTime.of(date, LocalTime.parse(timeStr));
                List<String> selectedSeats;

                if (isMeetingRoom) {
                    // No seats needed for meeting rooms
                    selectedSeats = new ArrayList<>();
                } else {
                    // Seat selection required
                    selectedSeats = seatButtons.stream()
                            .filter(AbstractButton::isSelected)
                            .map(AbstractButton::getText)
                            .collect(Collectors.toList());

                    if (!isMeetingRoom && !room.equals("Rehearsal Room") && selectedSeats.size() != size) {
                        groupStatus.setText("❌ You must select exactly " + size + " seats.");
                        return;
                    }

                }

                double price = isMeetingRoom
                        ? calculateMeetingRoomPrice(room, session)
                        : calculateGroupPrice(room, bookingTime, size);

                priceLabel.setText("Total Price: £" + String.format("%.2f", price));

                GroupBooking booking = new GroupBooking(
                        groupName.getText().trim() + " (" + room + ")",
                        size, bookingTime, selectedSeats);
                booking.setPrice(price);

                if (groupService.holdGroupBooking(booking)) {
                    groupStatus.setText("✅ Held successfully.");
                } else {
                    groupStatus.setText("❌ Booking failed.");
                }

            } catch (Exception ex) {
                groupStatus.setText("⚠️ Invalid input.");
            }
        });

        confirmBtn.addActionListener((ActionEvent e) -> {
            String fullName = groupName.getText().trim() + " (" + roomBox.getSelectedItem() + ")";
            if (groupService.confirmGroupBooking(fullName)) {
                groupService.getAllBookings().stream().filter(b -> b.getGroupName().equals(fullName) && "Confirmed".equals(b.getStatus())).findFirst().ifPresent(booking -> {
                    confirmedGroupBookings.add(booking);

                    groupModel.addRow(new Object[]{
                            booking.getGroupName(),
                            booking.getBookingTime().format(formatter),
                            booking.getGroupSize(),
                            String.join(", ", booking.getHeldRows()),
                            "£" + String.format("%.2f", booking.getPrice())
                    });
                            // ✅ SAVE to MySQL here
                    try {
                        MySQLDatabaseHelper.saveGroupBooking(
                                booking.getGroupName(),
                                booking.getGroupSize(),
                                Timestamp.valueOf(booking.getBookingTime()),
                                String.join(",", booking.getHeldRows()),
                                booking.getPrice(),
                                "Confirmed"
                        );
                        System.out.println("✅ Group booking saved to MySQL");
                    } catch (Exception ex) {
                        System.out.println("❌ Error saving group booking to MySQL:");
                        ex.printStackTrace();
                    }


                });
                groupStatus.setText("✅ Group confirmed.");
            } else groupStatus.setText("❌ No held booking.");
        });

        form.add(new JLabel("Group Name:")); form.add(groupName);
        form.add(new JLabel("Group Size:")); form.add(groupSize);
        form.add(new JLabel("Date:")); form.add(dateSpinner);
        form.add(new JLabel("Time:")); form.add(timeBox);
        form.add(new JLabel("Room:")); form.add(roomBox);
        panel.add(new JLabel("Session Type:")); panel.add(sessionBox); // ✅ <-- New line

        panel.add(sessionLabel); panel.add(sessionBox); // ✅ Add herepanel.add(sessionBox);
        panel.add(sessionLabel);
        form.add(priceLabel); form.add(groupStatus);
        form.add(holdBtn); form.add(confirmBtn);

        panel.add(form, BorderLayout.NORTH);
        panel.add(new JScrollPane(seatPanel), BorderLayout.CENTER);

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(ev -> {
            groupName.setText("");
            groupSize.setText("");
            roomBox.removeAllItems();
            timeBox.setSelectedIndex(-1);
            dateSpinner.setValue(new java.util.Date());
            priceLabel.setText("Total Price: £0.00");
            groupStatus.setText("");
            seatButtons.forEach(b -> b.setSelected(false));
            seatPanel.removeAll();
            seatPanel.revalidate();
            seatPanel.repaint();
        });
        form.add(refreshBtn); form.add(new JLabel()); // To fill grid slot

        return panel;
    }


    private JPanel createConfirmedPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        String[] groupHeaders = {"Group Name", "Date", "Size", "Held Seats", "Price"};
        groupModel = new DefaultTableModel(groupHeaders, 0);
        JTable groupTable = new JTable(groupModel);

        String[] singleHeaders = {"Customer Name", "Date", "Seat", "Price"};
        singleModel = new DefaultTableModel(singleHeaders, 0);
        JTable singleTable = new JTable(singleModel);

        panel.add(new JLabel("Confirmed Group Bookings:"));
        panel.add(new JScrollPane(groupTable));
        panel.add(new JLabel("Confirmed Single Bookings:"));
        panel.add(new JScrollPane(singleTable));
        return panel;
    }

    private JComboBox<String> createTimeDropdown() {
        JComboBox<String> timeDropdown = new JComboBox<>();
        IntStream.range(8, 24).forEach(hour -> {
            timeDropdown.addItem(String.format("%02d:00", hour));
            timeDropdown.addItem(String.format("%02d:30", hour));
        });
        return timeDropdown;
    }

    private boolean isSeatBooked(String seat, LocalDateTime time) {
        LocalDateTime start = time.minusHours(3);
        LocalDateTime end = time.plusHours(3);

        return confirmedSingleBookings.stream().anyMatch(b ->
                b.getSeatNumber().contains(seat) &&
                        !b.getBookingTime().isBefore(start) &&
                        !b.getBookingTime().isAfter(end)
        ) || confirmedGroupBookings.stream().anyMatch(b ->
                b.getHeldRows().contains(seat) &&
                        !b.getBookingTime().isBefore(start) &&
                        !b.getBookingTime().isAfter(end)
        );
    }


    private double calculateSinglePrice(String room, LocalDateTime time) {
        boolean weekend = time.getDayOfWeek() == DayOfWeek.FRIDAY || time.getDayOfWeek() == DayOfWeek.SATURDAY;
        int hour = time.getHour();
        switch (room) {
            case "Main Hall - Stalls":
            case "Main Hall - Balcony":
                return (hour >= 17 ? (weekend ? 12.00 : 10.00) : 6.50);
            case "Small Hall":
                return (hour >= 17 ? (weekend ? 9.00 : 7.00) : 5.00);
            case "Rehearsal Room":
                return 3.00;
            default:
                return 2.00;
        }
    }

    private double calculateGroupPrice(String room, LocalDateTime time, int size) {
        boolean weekend = time.getDayOfWeek() == DayOfWeek.FRIDAY || time.getDayOfWeek() == DayOfWeek.SATURDAY;
        int hour = time.getHour();
        boolean evening = hour >= 17;
        double basePrice;

        switch (room) {
            case "Main Hall - Stalls":
            case "Main Hall - Balcony":
                basePrice = evening ? (weekend ? 2200 : 1850) : 325 * 3;
                break;
            case "Small Hall":
                basePrice = evening ? (weekend ? 1300 : 950) : 225 * 3;
                break;
            case "Rehearsal Room":
                basePrice = evening ? (weekend ? 500 : 450) : 60 * 3;
                break;
            default:
                basePrice = 30 * size;
        }

        return basePrice * 1.2;
    }

    private double calculateMeetingRoomPrice(String room, String session) {
        int[] data = meetingRoomData.get(room); // [capacity, 1hr, halfDay, fullDay]
        switch (session) {
            case "1 Hour": return data[1] * 1.2;            // Add 20% VAT
            case "Morning/Afternoon": return data[2] * 1.2;
            case "All Day": return data[3] * 1.2;
            default: return 0;
        }
    }


    private List<String> generateMainHallSeats() {
        List<String> seats = new ArrayList<>();
        for (char row = 'A'; row <= 'Q'; row++) {
            for (int num = 1; num <= 19; num++) {
                seats.add("" + row + num); // ✅ becomes "A1", "B3", etc.

            }
        }
        for (int num = 1; num <= 53; num++) seats.add("AA" + num);
        for (int num = 1; num <= 23; num++) seats.add("BB" + num);
        for (int num = 1; num <= 8; num++) seats.add("CC" + num);
        return seats;
    }

    private List<String> generateSmallHallSeats() {
        List<String> seats = new ArrayList<>();
        for (char row = 'A'; row <= 'K'; row++) {
            for (int num = 1; num <= 12; num++) {
                seats.add("" + row + num); // ✅ becomes "A1", "B3", etc.

            }
        }
        return seats;
    }

    private List<String> autoAllocateSeats(int groupSize, LocalDateTime time, String room) {
        List<String> fullLayout;
        if (room.contains("Main Hall")) {
            fullLayout = generateMainHallSeats();
        } else if (room.contains("Small Hall")) {
            fullLayout = generateSmallHallSeats();
        } else {
            return new ArrayList<>();
        }

        List<String> available = filterAvailableSeats(fullLayout, time);

        for (int i = 0; i <= available.size() - groupSize; i++) {
            List<String> sub = available.subList(i, i + groupSize);
            if (areSeatsSequential(sub)) {
                return new ArrayList<>(sub);
            }
        }

        return new ArrayList<>();
    }

    private boolean areSeatsSequential(List<String> seats) {
        if (seats.isEmpty()) return false;
        String first = seats.get(0);
        boolean isDoubleAlpha = Character.isLetter(first.charAt(0)) && Character.isLetter(first.charAt(1));

        String row = isDoubleAlpha ? first.substring(0, 2) : first.substring(0, 1);

        for (String seat : seats) {
            if (!seat.startsWith(row)) return false;
        }

        List<Integer> numbers = seats.stream()
                .map(s -> Integer.parseInt(s.replaceAll("[^0-9]", "")))
                .sorted()
                .collect(Collectors.toList());

        for (int i = 1; i < numbers.size(); i++) {
            if (numbers.get(i) != numbers.get(i - 1) + 1) return false;
        }

        return true;
    }

    private List<String> filterAvailableSeats(List<String> seats, LocalDateTime time) {
        return seats.stream().filter(seat -> !isSeatBooked(seat, time)).collect(Collectors.toList());
    }

    private List<String> parseSeatRange(String input) {
        List<String> seats = new ArrayList<>();
        try {
            if (!input.contains("-")) return seats;
            String[] parts = input.split("-");
            String start = parts[0].toUpperCase().trim();
            String end = parts[1].toUpperCase().trim();
            char startRow = start.charAt(0);
            int startNum = Integer.parseInt(start.replaceAll("[^0-9]", ""));
            char endRow = end.charAt(0);
            int endNum = Integer.parseInt(end.replaceAll("[^0-9]", ""));

            for (char row = startRow; row <= endRow; row++) {
                for (int num = (row == startRow ? startNum : 1); num <= (row == endRow ? endNum : 20); num++) {
                    seats.add("" + row + num); // ✅ becomes "A1", "B3", etc.

                }
            }
        } catch (Exception e) {
            groupStatus.setText("⚠️ Invalid seat range format.");
        }
        return seats;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new BookingAppSwing().setVisible(true));
    }
}
