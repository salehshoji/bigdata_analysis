package ir.saleh;
import org.apache.kafka.clients.consumer.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;


public class LogConsumer {

    private static final int LIMITMIN = 5;
    private static final int COUNTLIMIT = 5;
    private static final int RATELIMIT = 10;

    public static void main(final String[] args) throws Exception {
        List<String> ERRORLIST = new ArrayList<>(Arrays.asList("ERROR", "WARNING"));
        final String topic = "purchases";
        // Load consumer configuration settings from a local file
        // Reusing the loadConfig method from the ProducerExample class
        final Properties props = loadConfig("src/main/resources/prop.properties");

        Map<String, List<Log>> componentMap = new HashMap();

        props.put(ConsumerConfig.GROUP_ID_CONFIG, "kafka-java-getting-started");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        final Consumer<String, String> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Arrays.asList(topic));
        while (true){
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
            for (ConsumerRecord<String, String> record : records) {
                String key = record.key();
                String value = record.value();
                for (String line : value.lines().toList()) {
                    Log log = logCreator(key, line);
                    if (!componentMap.containsKey(key)) {
                        componentMap.put(key, new ArrayList<>());
                    }
                    componentMap.get(key).add(log);
                    checkLogError(ERRORLIST, key, log);
                }
                System.out.println(componentMap);
                checkComponentProblems(componentMap);
            }
        }
    }

    private static void checkLogError(List<String> ERRORLIST, String key, Log log) {
        if (ERRORLIST.contains(log.getStatus())){
            // ERROR alert function
            // todo send Error to Table
            System.out.println(log.getStatus()+ "  " + key + "   " + log + "  ");
            System.out.println("this is test of datetime  " + log.getDateTime());
        }
    }

    private static void checkComponentProblems(Map<String, List<Log>> componentMap) {
        // rule 2
        for (String component : componentMap.keySet()) {
            List<Log> logList = componentMap.get(component);
            LocalDateTime startTime = null;
            int startIndex = 0;
            for (int i = 0; i < logList.size(); i++) {
                if(startTime == null){
                    startTime = logList.get(i).getDateTime();
                }else{
                    while (ChronoUnit.MINUTES.between(startTime, logList.get(i).getDateTime()) > LIMITMIN){
                        startIndex += 1;
                        startTime = logList.get(startIndex).getDateTime();
                    }
                    if (i - startIndex > COUNTLIMIT){
                        // todo send Error to Table
                        System.out.println("in component" + component + "from " + startTime + " to " + logList.get(i).getDateTime() + " we have " + (i - startIndex) + "error");
                    }
                }
            }
        }

        // rule 3
        for (String component : componentMap.keySet()){
            List<Log> logList = componentMap.get(component);
            if (logList.size() / (ChronoUnit.MINUTES.between(logList.get(0).getDateTime(), logList.get(logList.size() - 1).getDateTime())) > RATELIMIT){
                // todo send Error to table
                System.out.println("in component" + component + " reate is "
                        + (logList.size() / (ChronoUnit.MINUTES.between(logList.get(0).getDateTime(), logList.get(logList.size() - 1).getDateTime())))
                        + " and its more than " + RATELIMIT);
            }
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
        value = value.substring( value.indexOf(' ') + 1);
        String packageName = value.substring(0, value.indexOf(" "));
        value = value.substring(value.indexOf(' ') + 1);
        String className = value.substring(0, value.indexOf(" "));
        value = value.substring(value.indexOf(' ') + 1);
        String message = value.substring(value.indexOf("-") + 2);
        return new Log(key, datetime, logNum, threadName, status, packageName, className, message);
    }


    public static Properties loadConfig(final String configFile) throws IOException {
        if (!Files.exists(Paths.get(configFile))) {
            throw new IOException(configFile + " not found.");
        }
        final Properties cfg = new Properties();
        try (InputStream inputStream = new FileInputStream(configFile)) {
            cfg.load(inputStream);
        }
        return cfg;
    }
}