package ir.saleh;


import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import imported.*;
import jakarta.persistence.*;


@Entity
@Table(name = "Alerts")
public class Alert {
    public static List<Alert> alertsList = new ArrayList<>();

    public String getComponentName() {
        return componentName;
    }

    public String getAlertName() {
        return alertName;
    }

    public String getDescription() {
        return description;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    public void setAlertName(String alertName) {
        this.alertName = alertName;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Id
    String componentName;
    String alertName;
    String description;

    public Alert(String componentName, String alertName, String description) {
        this.componentName = componentName;
        this.alertName = alertName;
        this.description = description;
        alertsList.add(this);
    }

    public Alert() {
    }

    public void pushToDatabase() throws SQLException {
        String connectionUrl = "jdbc:mysql://localhost:3306/database_saleh";
        Connection conn = DriverManager.getConnection(connectionUrl);
        checkTable(conn, "alerts");
        Statement stmt = conn.createStatement();
        String sql = "INSERT INTO alerts VALUES (" + "\"" + this.componentName + "\"" + ", " + "\"" + this.alertName +
                "\"" + ", " + "\"" + this.description + "\"" + ")";
        stmt.executeUpdate(sql);
        showDB();
    }

    private void checkTable(Connection conn, String alerts) throws SQLException {
        if (!tableExists(conn, "alerts")) {
            String sql = "CREATE TABLE alerts " +
                    "(" +
                    " component_name VARCHAR(255) not NULL, " +
                    " alert_name VARCHAR(255), " +
                    " description VARCHAR(255))";
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(sql);
        }
    }

    public static void showDB() throws SQLException {
        String connectionUrl = "jdbc:mysql://localhost:3306/database_saleh";
        Connection conn = DriverManager.getConnection(connectionUrl);

        // Just pass the connection and the table name to printTable()
        DBTablePrinter.printTable(conn, "alerts");
    }

    public static boolean tableExists(Connection connection, String tableName) throws SQLException {
        DatabaseMetaData meta = connection.getMetaData();
        ResultSet resultSet = meta.getTables(null, null, tableName, new String[]{"TABLE"});

        return resultSet.next();
    }
}
