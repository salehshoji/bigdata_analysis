package ir.saleh;
import org.apache.kafka.clients.consumer.*;
import org.json.simple.JSONObject;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.*;


public class LogConsumer {
    public static void main(final String[] args) throws Exception {
        List<String> ERRORLIST = new ArrayList<>(Arrays.asList("ERROR", "WARNING"));
        final String topic = "purchases";
        // Load consumer configuration settings from a local file
        // Reusing the loadConfig method from the ProducerExample class
        final Properties props = loadConfig("src/main/resources/prop.properties");

        List<List<JSONObject>> all_logs = new ArrayList<>();
        Map<String, Integer> componentMap = new HashMap();

        props.put(ConsumerConfig.GROUP_ID_CONFIG, "kafka-java-getting-started");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        final Consumer<String, String> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Arrays.asList(topic));
        while (true){
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
            for (ConsumerRecord<String, String> record : records) {
                String key = record.key();
                String value = record.value();
                Log log = logCreator(key, value);
                if (!componentMap.containsKey(key)){
                    componentMap.put(key, componentMap.size());
                    all_logs.add(new ArrayList<>());
                }
//                all_logs.get(componentMap.get(key)).add(log);

//                System.out.println(all_logs);
//                System.out.println(componentMap);
                if (ERRORLIST.contains(log.getStatus())){
                    // ERROR alert function
                    System.out.println(log.getStatus()+ "  " + key + "   " + log + "  ");
                }
                System.out.println(all_logs);
                checkComponentProblems(all_logs);
            }
        }
    }

    private static void checkComponentProblems(List<List<JSONObject>> allLogs) {
        for (int i = 0; i < allLogs.size(); i++) {
            List data_time = new ArrayList();
            for(JSONObject log : allLogs.get(i)){
                data_time.add(log.get("datetime"));
            }
            System.out.println(data_time);
        }
    }

    private static Log logCreator(String key, String value) {
        String datetime = value.substring(0, value.indexOf(","));
        String logNum = value.substring(0, value.indexOf(" "));
        String threadName = value.substring(0, value.indexOf(" "));
        String status = value.substring(0, value.indexOf(" "));
        String packageName = value.substring(0, value.indexOf(" "));
        String className = value.substring(0, value.indexOf(" "));
        String message = value.substring(value.indexOf("-") + 2);
        return new Log(datetime, logNum, threadName, status, packageName, className, message, key);
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