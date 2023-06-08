package ir.saleh;


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
}
