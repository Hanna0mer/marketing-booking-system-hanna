// BookingApp.java with confirmed booking price display for single and group bookings
package marketing;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import marketing.model.GroupBooking;
import marketing.model.SingleBooking;
import marketing.service.GroupBookingService;
import marketing.service.SingleBookingService;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class BookingApp extends Application {

    private final GroupBookingService groupService = new GroupBookingService();
    private final SingleBookingService singleService = new SingleBookingService();
    private final ObservableList<GroupBooking> confirmedGroupBookings = FXCollections.observableArrayList();
    private final ObservableList<SingleBooking> confirmedSingleBookings = FXCollections.observableArrayList();

    private ComboBox<String> createTimeDropdown() {
        ComboBox<String> timeDropdown = new ComboBox<>();
        IntStream.range(8, 24).forEach(hour -> {
            timeDropdown.getItems().add(String.format("%02d:00", hour));
            timeDropdown.getItems().add(String.format("%02d:30", hour));
        });
        timeDropdown.setPromptText("Select Time");
        return timeDropdown;
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

    // Generate seat numbers based on selected room
    private List<String> generateSeatsForRoom(String room) {
        List<String> seats = new ArrayList<>();
        if (room.contains("Main Hall")) {
            for (char row = 'A'; row <= 'L'; row++) {
                for (int num = 1; num <= 20; num++) {
                    seats.add(row + String.valueOf(num));
                }
            }
        } else if (room.contains("Small Hall")) {
            for (int i = 1; i <= 95; i++) {
                seats.add("S" + i);
            }
        } else if (room.contains("Rehearsal")) {
            for (int i = 1; i <= 30; i++) {
                seats.add("R" + i);
            }
        } else {
            for (int i = 1; i <= 20; i++) {
                seats.add("M" + i);
            }
        }
        return seats;
    }

    // Calculate group booking price based on room and time
    private double calculateGroupPrice(String room, LocalDateTime time, int size) {
        boolean weekend = time.getDayOfWeek() == DayOfWeek.FRIDAY || time.getDayOfWeek() == DayOfWeek.SATURDAY;
        int hour = time.getHour();

        switch (room) {
            case "Main Hall - Stalls":
            case "Main Hall - Balcony":
                return (hour >= 17 ? (weekend ? 2200 : 1850) : 325 * 3);
            case "Small Hall":
                return (hour >= 17 ? (weekend ? 1300 : 950) : 225 * 3);
            case "Rehearsal Room":
                return (hour >= 17 ? (weekend ? 500 : 450) : 60 * 3);
            default:
                return 30 * size; // flat estimate for meeting rooms
        }
    }

    @Override
    public void start(Stage primaryStage) {
        TabPane tabPane = new TabPane();

        // --- Single Booking Tab ---
        VBox singleForm = new VBox(10);
        TextField customerNameField = new TextField();
        customerNameField.setPromptText("Customer Name");
        DatePicker singleDate = new DatePicker();
        ComboBox<String> singleTime = createTimeDropdown();
        ComboBox<String> singleRoom = new ComboBox<>(FXCollections.observableArrayList("Main Hall - Stalls", "Main Hall - Balcony", "Small Hall", "Rehearsal Room"));
        singleRoom.setPromptText("Select Room");
        TextField seatField = new TextField();
        seatField.setPromptText("Seat Number (e.g. A1)");
        CheckBox wheelchairBox = new CheckBox("Wheelchair Accessible");
        Button confirmSingleBtn = new Button("Confirm Single Booking");
        Label singleStatus = new Label();

        confirmSingleBtn.setOnAction(e -> {
            try {
                LocalDate date = singleDate.getValue();
                String timeStr = singleTime.getValue();
                if (date == null || timeStr == null) {
                    singleStatus.setText("⚠️ Select date and time."); return;
                }
                LocalDateTime bookingTime = LocalDateTime.of(date, LocalTime.parse(timeStr));
                String name = customerNameField.getText().trim();
                String seat = seatField.getText().trim();
                String room = singleRoom.getValue();
                boolean wheelchair = wheelchairBox.isSelected();

                if (room == null || room.isEmpty()) { singleStatus.setText("⚠️ Select a room."); return; }
                if (seat.matches("R.*")) { singleStatus.setText("❌ Restricted view seat."); return; }
                if (wheelchair && !(seat.startsWith("A") || seat.startsWith("L") || seat.endsWith("1") || seat.endsWith("10"))) {
                    singleStatus.setText("⚠️ Accessible seats only on Row A/L or edges."); return;
                }

                double price = calculateSinglePrice(room, bookingTime);
                SingleBooking booking = new SingleBooking(name, bookingTime, seat + " (" + room + ")");
                booking.setPrice(price);
                if (singleService.confirmSingleBooking(booking)) {
                    confirmedSingleBookings.add(booking);
                    singleStatus.setText("✅ Booking confirmed. Price: £" + String.format("%.2f", price));
                } else {
                    singleStatus.setText("❌ Seat taken.");
                }
            } catch (Exception ex) {
                singleStatus.setText("⚠️ Invalid input.");
            }
        });

        singleForm.getChildren().addAll(customerNameField, singleDate, singleTime, singleRoom, seatField, wheelchairBox, confirmSingleBtn, singleStatus);
        Tab singleTab = new Tab("Single Booking", singleForm);

        // --- Group Booking Tab ---
        VBox groupForm = new VBox(10);
        TextField groupName = new TextField();
        groupName.setPromptText("Group Name");
        TextField groupSize = new TextField();
        groupSize.setPromptText("Group Size");
        DatePicker groupDate = new DatePicker();
        ComboBox<String> groupTime = createTimeDropdown();
        ComboBox<String> groupRoom = new ComboBox<>(FXCollections.observableArrayList("Main Hall - Stalls", "Main Hall - Balcony", "Small Hall", "Rehearsal Room", "Meeting Room 1", "Meeting Room 2", "Meeting Room 3", "Meeting Room 4", "Meeting Room 5"));
        groupRoom.setPromptText("Select Room");
        ListView<String> seatSelector = new ListView<>();
        seatSelector.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        Label priceLabel = new Label("Total Price: £0.00");

        groupRoom.setOnAction(e -> {
            seatSelector.getItems().clear();
            if (groupRoom.getValue() != null) {
                seatSelector.getItems().addAll(generateSeatsForRoom(groupRoom.getValue()));
            }
        });

        Button holdBtn = new Button("Hold Group Booking");
        Button confirmBtn = new Button("Confirm Group Booking");
        Label groupStatus = new Label();

        holdBtn.setOnAction(e -> {
            try {
                int size = Integer.parseInt(groupSize.getText().trim());
                LocalDate date = groupDate.getValue();
                String timeStr = groupTime.getValue();
                if (size < 12) { groupStatus.setText("❌ Group size must be 12+"); return; }
                if (date == null || timeStr == null) { groupStatus.setText("⚠️ Select date and time."); return; }
                String room = groupRoom.getValue();
                if (room == null || room.isEmpty()) { groupStatus.setText("⚠️ Select a room."); return; }
                List<String> selectedSeats = seatSelector.getSelectionModel().getSelectedItems();
                if (selectedSeats.isEmpty()) {
                    groupStatus.setText("❌ Select seats to hold."); return;
                }
                if (selectedSeats.stream().anyMatch(r -> r.trim().matches("R.*"))) {
                    groupStatus.setText("❌ Cannot hold restricted view seats."); return;
                }
                LocalDateTime bookingTime = LocalDateTime.of(date, LocalTime.parse(timeStr));
                double price = calculateGroupPrice(room, bookingTime, size);
                priceLabel.setText("Total Price: £" + String.format("%.2f", price));
                GroupBooking booking = new GroupBooking(groupName.getText().trim() + " (" + room + ")", size, bookingTime, selectedSeats);
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

        confirmBtn.setOnAction(e -> {
            String name = groupName.getText().trim();
            if (groupService.confirmGroupBooking(name)) {
                groupService.getAllBookings().stream()
                        .filter(b -> b.getGroupName().equals(name) && "Confirmed".equals(b.getStatus()))
                        .findFirst().ifPresent(confirmedGroupBookings::add);
                groupStatus.setText("✅ Group confirmed.");
            } else {
                groupStatus.setText("❌ No held booking.");
            }
        });

        groupForm.getChildren().addAll(groupName, groupSize, groupDate, groupTime, groupRoom, seatSelector, priceLabel, holdBtn, confirmBtn, groupStatus);
        Tab groupTab = new Tab("Group Booking", groupForm);


        VBox confirmedView = new VBox(10);

        Label groupLabel = new Label("Confirmed Group Bookings:");
        TableView<GroupBooking> groupTable = new TableView<>(confirmedGroupBookings);
        TableColumn<GroupBooking, String> groupNameCol = new TableColumn<>("Group Name");
        groupNameCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getGroupName()));
        TableColumn<GroupBooking, String> groupDateCol = new TableColumn<>("Date");
        groupDateCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getBookingTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));
        TableColumn<GroupBooking, String> groupSizeCol = new TableColumn<>("Size");
        groupSizeCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(String.valueOf(d.getValue().getGroupSize())));
        TableColumn<GroupBooking, String> heldRowsCol = new TableColumn<>("Held Seats");
        heldRowsCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(String.join(", ", d.getValue().getHeldRows())));
        TableColumn<GroupBooking, String> groupPriceCol = new TableColumn<>("Price");
        groupPriceCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty("£" + String.format("%.2f", d.getValue().getPrice())));
        groupTable.getColumns().addAll(groupNameCol, groupDateCol, groupSizeCol, heldRowsCol, groupPriceCol);

        Label singleLabel = new Label("Confirmed Single Bookings:");
        TableView<SingleBooking> singleTable = new TableView<>(confirmedSingleBookings);
        TableColumn<SingleBooking, String> customerCol = new TableColumn<>("Customer Name");
        customerCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getCustomerName()));
        TableColumn<SingleBooking, String> singleDateCol = new TableColumn<>("Date");
        singleDateCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getBookingTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));
        TableColumn<SingleBooking, String> seatCol = new TableColumn<>("Seat");
        seatCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getSeatNumber()));
        TableColumn<SingleBooking, String> singlePriceCol = new TableColumn<>("Price");
        singlePriceCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty("£" + String.format("%.2f", d.getValue().getPrice())));
        singleTable.getColumns().addAll(customerCol, singleDateCol, seatCol, singlePriceCol);

        confirmedView.getChildren().addAll(groupLabel, groupTable, singleLabel, singleTable);
        Tab confirmedTab = new Tab("Confirmed Bookings", confirmedView);

        tabPane.getTabs().addAll(singleTab, groupTab, confirmedTab);
        primaryStage.setScene(new Scene(tabPane, 850, 650));
        primaryStage.setTitle("Marketing Booking System");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
