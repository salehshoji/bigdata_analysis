package ir.saleh.rest;


import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;


@Entity
@Table(name = "Alerts")
public class Alert {

    @Id
    private int id;
    private String componentName;
    private String alertName;
    private String description;

    public static List<Alert> alertsList = new ArrayList<>();
    private static int countAlert = 0;

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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Alert(String componentName, String alertName, String description) {
        this.id = countAlert;
        countAlert += 1;
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
        String sql = "INSERT INTO alerts (id, component_name, alert_name, description) VALUES (" +
                this.id + ",\"" + this.componentName + "\"" + ", " + "\"" + this.alertName +
                "\"" + ", " + "\"" + this.description + "\"" + ")";
        stmt.executeUpdate(sql);
    }

    private void checkTable(Connection conn, String alerts) throws SQLException {
        if (!tableExists(conn, "alerts")) {
            String sql = "CREATE TABLE alerts " +
                    "(" +
                    "id int PRIMARY KEY," +
                    " component_name VARCHAR(255) not NULL, " +
                    " alert_name VARCHAR(255), " +
                    " description VARCHAR(255))";
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(sql);
        }
    }


    public static boolean tableExists(Connection connection, String tableName) throws SQLException {
        DatabaseMetaData meta = connection.getMetaData();
        ResultSet resultSet = meta.getTables(null, null, tableName, new String[]{"TABLE"});

        return resultSet.next();
    }
}
