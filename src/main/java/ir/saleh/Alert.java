package ir.saleh;


import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Alert {
    public static List<Alert> alertsList = new ArrayList<>();
    String componentName;
    String alertName;
    String description;

    public Alert(String componentName, String alertName, String description) {
        this.componentName = componentName;
        this.alertName = alertName;
        this.description = description;
        alertsList.add(this);
    }

    public static void main(String[] args) throws SQLException {
        DB();
    }
    public static void DB() throws SQLException {
//        String selectDB = "USE database_saleh";
        String sqlSelectAllPersons = "SELECT * FROM person";
        String connectionUrl = "jdbc:mysql://localhost:3306/database_saleh";
        Connection conn = DriverManager.getConnection(connectionUrl);
//        conn.setCatalog("database_saleh");
//        PreparedStatement ps = conn.prepareStatement(selectDB);
//        ResultSet rs = ps.executeQuery();
        PreparedStatement ps = conn.prepareStatement(sqlSelectAllPersons);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            long id = rs.getLong("ID");
            String name = rs.getString("FIRST_NAME");
            String lastName = rs.getString("LAST_NAME");
            System.out.println(id + "||" + name + "||" + lastName);
            // do something with the extracted data...
        }
    }
}
