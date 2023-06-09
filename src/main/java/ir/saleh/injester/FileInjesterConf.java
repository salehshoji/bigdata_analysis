package ir.saleh.injester;

public class FileInjesterConf {
    private String logPath;
    private String logDestPath;
    private String topic;
    private String kafkaPropertiesPath;

    public String getLogPath() {
        return logPath;
    }

    public void setLogPath(String logPath) {
        this.logPath = logPath;
    }

    public String getLogDestPath() {
        return logDestPath;
    }

    public void setLogDestPath(String logDestPath) {
        this.logDestPath = logDestPath;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getKafkaPropertiesPath() {
        return kafkaPropertiesPath;
    }

    public void setKafkaPropertiesPath(String kafkaPropertiesPath) {
        this.kafkaPropertiesPath = kafkaPropertiesPath;
    }


}
