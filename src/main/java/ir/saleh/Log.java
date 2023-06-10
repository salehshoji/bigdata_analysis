package ir.saleh;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

public class Log {
    private LocalDateTime dateTime;
    private String threadName;
    private String status;
    private String packageName;
    private String classname;
    private String message;
    private String component;

    public Log(String component, String dateTime, String threadName,
               String status, String packageName, String classname, String message) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        this.dateTime = LocalDateTime.parse(dateTime, formatter);
        this.threadName = threadName;
        this.status = status;
        this.packageName = packageName;
        this.classname = classname;
        this.message = message;
        this.component = component;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }


    public String getThreadName() {
        return threadName;
    }

    public String getStatus() {
        return status;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getClassname() {
        return classname;
    }

    public String getMessage() {
        return message;
    }

    public String getComponent() {
        return component;
    }

}
