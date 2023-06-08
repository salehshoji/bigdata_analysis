package ir.saleh;


import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import imported.*;

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
        try {
            pushToDatabase(this);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void pushToDatabase(Alert alert) throws SQLException {
        String connectionUrl = "jdbc:mysql://localhost:3306/database_saleh";
        Connection conn = DriverManager.getConnection(connectionUrl);
        checkTable(conn, "Alerts");
        Statement stmt = conn.createStatement();
        String sql = "INSERT INTO Alerts VALUES (" + "\"" + alert.componentName + "\"" + ", " + "\"" + alert.alertName +
                "\"" + ", " + "\"" + alert.description + "\"" + ")";
        stmt.executeUpdate(sql);
        showDB();
    }

    private void checkTable(Connection conn, String alerts) throws SQLException {
        if(!tableExists(conn, "Alerts")){
            String sql = "CREATE TABLE Alerts " +
                    "(" +
                    " component VARCHAR(255) not NULL, " +
                    " Alert VARCHAR(255), " +
                    " message VARCHAR(255))";
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(sql);
        }
    }

    public static void showDB() throws SQLException {
        String connectionUrl = "jdbc:mysql://localhost:3306/database_saleh";
        Connection conn = DriverManager.getConnection(connectionUrl);

//        String sqlSelectAllPersons = "SELECT * FROM Alerts";
//        PreparedStatement ps = conn.prepareStatement(sqlSelectAllPersons);
//        ResultSet rs = ps.executeQuery();
//        while (rs.next()) {
//            String id = rs.getString("component");
//            String name = rs.getString("Alert");
//            String lastName = rs.getString("message");
//            System.out.println(id + "||" + name + "||" + lastName);
//            // do something with the extracted data...
//        }
    // Just pass the connection and the table name to printTable()
        DBTablePrinter.printTable(conn, "Alerts");
    }

    public static boolean tableExists(Connection connection, String tableName) throws SQLException {
        DatabaseMetaData meta = connection.getMetaData();
        ResultSet resultSet = meta.getTables(null, null, tableName, new String[] {"TABLE"});

        return resultSet.next();
    }
}
