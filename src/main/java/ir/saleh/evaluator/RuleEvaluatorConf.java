package ir.saleh.evaluator;

import java.util.List;

public class RuleEvaluatorConf {
    private float duration;
    private float countLimit;
    private float rateLimit;

    private List<String> errorList;
    private String topic;

    private String kafkaPropertiesPath;
    private String kafkaGroupIdConfig;
    private String kafkaAutoOffsetResetConfig;

    public float getDuration() {
        return duration;
    }

    public void setDuration(float duration) {
        this.duration = duration;
    }

    public float getCountLimit() {
        return countLimit;
    }

    public void setCountLimit(float countLimit) {
        this.countLimit = countLimit;
    }

    public float getRateLimit() {
        return rateLimit;
    }

    public void setRateLimit(float rateLimit) {
        this.rateLimit = rateLimit;
    }

    public List<String> getErrorList() {
        return errorList;
    }

    public void setErrorList(List<String> errorList) {
        this.errorList = errorList;
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

    public String getKafkaGroupIdConfig() {
        return kafkaGroupIdConfig;
    }

    public void setKafkaGroupIdConfig(String kafkaGroupIdConfig) {
        this.kafkaGroupIdConfig = kafkaGroupIdConfig;
    }

    public String getKafkaAutoOffsetResetConfig() {
        return kafkaAutoOffsetResetConfig;
    }

    public void setKafkaAutoOffsetResetConfig(String kafkaAutoOffsetResetConfig) {
        this.kafkaAutoOffsetResetConfig = kafkaAutoOffsetResetConfig;
    }
}
