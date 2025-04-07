package marketing.db;

import java.sql.*;

public class MySQLDatabaseHelper {
    private static final String URL = "jdbc:mysql://sst-stuproj.city.ac.uk:3306/in2033t21";
    private static final String USER = "in2033t21_a";
    private static final String PASSWORD = "lrLUWCLVzDQ";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // Save to group_bookings table
    public static void saveGroupBooking(String groupName, int groupSize, Timestamp bookingTime, String heldRows, double price, String status) {
        String sql = "INSERT INTO group_bookings (group_name, group_size, booking_time, held_rows, price, status) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, groupName);
            stmt.setInt(2, groupSize);
            stmt.setTimestamp(3, bookingTime);
            stmt.setString(4, heldRows); // e.g., "A1,A2,A3"
            stmt.setDouble(5, price);
            stmt.setString(6, status);   // e.g., "Confirmed"

            stmt.executeUpdate();
            System.out.println("✅ Group booking saved to MySQL: " + groupName + ", " + heldRows);
        } catch (SQLException e) {
            System.out.println("❌ Error saving group booking:");
            e.printStackTrace();
        }
    }


    // Save to single_bookings table
    public static void saveSingleBooking(String customerName, Timestamp bookingTime, String seatNumber, double price) {
        String sql = "INSERT INTO single_bookings (customer_name, booking_time, seat_number, price) VALUES (?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, customerName);
            pstmt.setTimestamp(2, bookingTime);
            pstmt.setString(3, seatNumber);
            pstmt.setDouble(4, price);
            pstmt.executeUpdate();
            System.out.println("✅ Single booking saved to MySQL: " + customerName + ", " + seatNumber);
        } catch (SQLException e) {
            System.err.println("❌ Error saving single booking:");
            e.printStackTrace();
            throw new RuntimeException("Failed to save single booking", e); // Temporarily throw to catch in app
        }
    }
}