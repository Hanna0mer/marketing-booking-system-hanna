package marketing;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import marketing.model.Film;
import marketing.model.GroupBooking;
import marketing.service.FilmSchedulerService;
import marketing.service.GroupBookingService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

public class BookingApp extends Application {

    private final FilmSchedulerService filmService = new FilmSchedulerService();
    private final GroupBookingService groupService = new GroupBookingService();
    private final ObservableList<GroupBooking> confirmedBookings = FXCollections.observableArrayList();
    private final ObservableList<Film> scheduledFilms = FXCollections.observableArrayList();

    @Override
    public void start(Stage primaryStage) {
        TabPane tabPane = new TabPane();

        // Film Scheduler Tab
        VBox filmForm = new VBox(10);
        TextField titleField = new TextField();
        titleField.setPromptText("Film Title (e.g. Mission2025)");

        TextField dateField = new TextField();
        dateField.setPromptText("Screening Date (dd/MM/yyyy HH:mm)");

        TextField costField = new TextField();
        costField.setPromptText("Cost");

        TextField seatsField = new TextField();
        seatsField.setPromptText("Available Seats");

        Button scheduleButton = new Button("Schedule Film");
        Label filmStatus = new Label();

        scheduleButton.setOnAction(e -> {
            try {
                String title = titleField.getText().trim();
                LocalDateTime screeningTime = LocalDateTime.parse(dateField.getText().trim(),
                        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
                double cost = Double.parseDouble(costField.getText().trim());
                int seats = Integer.parseInt(seatsField.getText().trim());

                Film film = new Film(title, screeningTime, cost, seats);
                boolean scheduled = filmService.scheduleFilm(film);

                if (scheduled) {
                    filmStatus.setText("✅ Film scheduled successfully.");
                    scheduledFilms.add(film); // Add to observable list
                } else {
                    filmStatus.setText("❌ Time slot unavailable.");
                }
            } catch (Exception ex) {
                filmStatus.setText("⚠️ Invalid input. Please try again.");
            }
        });

        filmForm.getChildren().addAll(titleField, dateField, costField, seatsField, scheduleButton, filmStatus);
        filmForm.setStyle("-fx-padding: 20;");
        Tab filmTab = new Tab("Schedule Film", filmForm);

        // Group Booking Tab
        VBox groupForm = new VBox(10);
        TextField groupNameField = new TextField();
        groupNameField.setPromptText("Group Name");

        TextField groupSizeField = new TextField();
        groupSizeField.setPromptText("Group Size (must be 12 or more)");

        TextField groupTimeField = new TextField();
        groupTimeField.setPromptText("Booking Date (dd/MM/yyyy HH:mm)");

        TextField heldRowsField = new TextField();
        heldRowsField.setPromptText("Held Rows (comma-separated e.g. A1,A2)");

        Button holdGroupButton = new Button("Hold Group Booking");
        Button confirmGroupButton = new Button("Confirm Group Booking");
        Label groupStatus = new Label();

        holdGroupButton.setOnAction(e -> {
            try {
                String name = groupNameField.getText().trim();
                int size = Integer.parseInt(groupSizeField.getText().trim());
                LocalDateTime time = LocalDateTime.parse(groupTimeField.getText().trim(),
                        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
                String[] heldRows = heldRowsField.getText().trim().split(",");
                GroupBooking booking = new GroupBooking(name, size, time, Arrays.asList(heldRows));

                boolean held = groupService.holdGroupBooking(booking);
                if (held) {
                    groupStatus.setText("✅ Group booking held.");
                } else {
                    groupStatus.setText("❌ Group size must be 12 or more.");
                }
            } catch (Exception ex) {
                groupStatus.setText("⚠️ Invalid input. Please try again.");
            }
        });

        confirmGroupButton.setOnAction(e -> {
            String name = groupNameField.getText().trim();
            boolean confirmed = groupService.confirmGroupBooking(name);
            if (confirmed) {
                groupStatus.setText("✅ Group booking confirmed.");
                groupService.getAllBookings().stream()
                        .filter(b -> b.getGroupName().equals(name) && "Confirmed".equals(b.getStatus()))
                        .findFirst()
                        .ifPresent(confirmedBookings::add);
            } else {
                groupStatus.setText("❌ No held booking found for that group name.");
            }
        });

        groupForm.getChildren().addAll(groupNameField, groupSizeField, groupTimeField, heldRowsField,
                holdGroupButton, confirmGroupButton, groupStatus);
        groupForm.setStyle("-fx-padding: 20;");
        Tab groupTab = new Tab("Group Booking", groupForm);

        // Confirmed Bookings Tab
        TableView<GroupBooking> bookingTable = new TableView<>(confirmedBookings);
        TableColumn<GroupBooking, String> nameCol = new TableColumn<>("Group Name");
        nameCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getGroupName()));

        TableColumn<GroupBooking, String> sizeCol = new TableColumn<>("Size");
        sizeCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(String.valueOf(data.getValue().getGroupSize())));

        TableColumn<GroupBooking, String> timeCol = new TableColumn<>("Date");
        timeCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().getBookingTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));

        TableColumn<GroupBooking, String> rowsCol = new TableColumn<>("Held Rows");
        rowsCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                String.join(", ", data.getValue().getHeldRows())));

        bookingTable.getColumns().addAll(nameCol, sizeCol, timeCol, rowsCol);
        VBox bookingView = new VBox(bookingTable);
        bookingView.setStyle("-fx-padding: 20;");
        Tab confirmedTab = new Tab("Confirmed Bookings", bookingView);

        // Scheduled Films Tab (Live Updating)
        TableView<Film> filmTable = new TableView<>(scheduledFilms);
        TableColumn<Film, String> filmTitleCol = new TableColumn<>("Title");
        filmTitleCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getTitle()));

        TableColumn<Film, String> filmTimeCol = new TableColumn<>("Screening Time");
        filmTimeCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().getScreeningTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));

        TableColumn<Film, String> filmCostCol = new TableColumn<>("Cost");
        filmCostCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(String.valueOf(data.getValue().getCost())));

        TableColumn<Film, String> filmSeatsCol = new TableColumn<>("Seats");
        filmSeatsCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(String.valueOf(data.getValue().getAvailableSeats())));

        filmTable.getColumns().addAll(filmTitleCol, filmTimeCol, filmCostCol, filmSeatsCol);
        VBox filmView = new VBox(filmTable);
        filmView.setStyle("-fx-padding: 20;");
        Tab scheduledTab = new Tab("Scheduled Films", filmView);

        // Add all tabs
        tabPane.getTabs().addAll(filmTab, groupTab, confirmedTab, scheduledTab);
        Scene scene = new Scene(tabPane, 600, 450);
        primaryStage.setTitle("Marketing Booking System");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
