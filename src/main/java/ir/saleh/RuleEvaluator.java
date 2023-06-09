package ir.saleh;

import org.apache.kafka.clients.consumer.*;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.InputStream;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;


public class RuleEvaluator {

    private static float DURATION; // second
    private static float COUNT_LIMIT; // number in TIME_LIMIT
    private static float RATE_LIMIT; // log per second
    private static List<String> ERROR_LIST;

    public static void main(final String[] args) throws Exception {
        final Yaml yaml = new Yaml(new Constructor(RuleEvaluatorConf.class));
        InputStream inputStream = RuleEvaluator.class.getClassLoader()
                .getResourceAsStream("configs/rule-evaluator.yml");
        RuleEvaluatorConf ruleEvaluatorconf = yaml.load(inputStream);

        final String topic = ruleEvaluatorconf.getTopic();
        DURATION = ruleEvaluatorconf.getDuration();
        COUNT_LIMIT = ruleEvaluatorconf.getCountLimit();
        RATE_LIMIT = ruleEvaluatorconf.getRateLimit();
        ERROR_LIST = ruleEvaluatorconf.getErrorList();

        // Load consumer configuration settings from a local file
        final Properties props = FileInjester.loadConfig(ruleEvaluatorconf.getKafkaPropertiesPath());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, ruleEvaluatorconf.getKafkaGroupIdConfig());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, ruleEvaluatorconf.getKafkaAutoOffsetResetConfig());
        final Consumer<String, String> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(List.of(topic));

        Map<String, List<Log>> componentMap = new HashMap<>();

        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
            for (ConsumerRecord<String, String> record : records) {
                String key = record.key();
                String value = record.value();
                for (String line : value.lines().toList()) {
                    Log log = logCreator(key, line);
                    if (!componentMap.containsKey(key)) {
                        componentMap.put(key, new ArrayList<>());
                    }
                    List<Log> logList = componentMap.get(key);
                    logList.add(log);
                    checkLogType(key, log);
                    while (ChronoUnit.SECONDS.between(logList.get(0).getDateTime(), log.getDateTime()) > DURATION) {
                        logList.remove(0);
                    }
                    checkComponentProblems(logList, key);
                }
            }
        }
    }

    private static void checkLogType(String key, Log log) {
        if (ERROR_LIST.contains(log.getStatus())) {
            // ERROR alert function
            new Alert(key, "first_rule",
                    "rule1" + log.getStatus() + "  " + key + "   " + log + " on " + log.getDateTime());
        }
    }

    private static void checkComponentProblems(List<Log> logList, String component) {
        // rule 2
        LocalDateTime startTime = null;
        int startIndex = 0;
        for (int i = 0; i < logList.size(); i++) {
            if (startTime == null) {
                startTime = logList.get(i).getDateTime();
            } else {
                while (ChronoUnit.SECONDS.between(startTime, logList.get(i).getDateTime()) > DURATION) {
                    startIndex += 1;
                    startTime = logList.get(startIndex).getDateTime();
                }
                if (i - startIndex > COUNT_LIMIT) {
                    new Alert(component, "second_alert",
                            ("rule2 in component" + component + "from " + startTime + " to "
                                    + logList.get(i).getDateTime() + " we have " + (i - startIndex) + "error"));
                }
            }
        }

        // rule 3
        if ((ChronoUnit.SECONDS.between(logList.get(0).getDateTime(), logList.get(logList.size() - 1).getDateTime()) == 0)) {
            if (logList.size() > RATE_LIMIT) {
                new Alert(component, "third_rule",
                        "rule 3 in component" + component + " rate is "
                                + (logList.size()) + "in less than second!!!" + " and its more than " + RATE_LIMIT);
            }
        } else if (logList.size() / (ChronoUnit.SECONDS.between(logList.get(0).getDateTime(), logList.get(logList.size() - 1).getDateTime())) > RATE_LIMIT) {
            new Alert(component, "third_rule",
                    "rule 3 in component" + component + " rate is "
                            + (logList.size() / (ChronoUnit.SECONDS.between(logList.get(0).getDateTime(), logList.get(logList.size() - 1).getDateTime())))
                            + " and its more than " + RATE_LIMIT);
        }

    }

    private static Log logCreator(String key, String value) {
        String datetime = value.substring(0, value.indexOf("."));
        value = value.substring(value.indexOf(",") + 1);
        String logNum = value.substring(0, value.indexOf(" "));
        value = value.substring(value.indexOf(' ') + 1);
        String threadName = value.substring(0, value.indexOf(" "));
        value = value.substring(value.indexOf(' ') + 1);
        String status = value.substring(0, value.indexOf(" "));
        value = value.substring(value.indexOf(' ') + 1);
        String packageName = value.substring(0, value.indexOf(" "));
        value = value.substring(value.indexOf(' ') + 1);
        String className = value.substring(0, value.indexOf(" "));
        value = value.substring(value.indexOf(' ') + 1);
        String message = value.substring(value.indexOf("-") + 2);
        return new Log(key, datetime, logNum, threadName, status, packageName, className, message);
    }


}