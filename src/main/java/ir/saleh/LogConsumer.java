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
                JSONObject log = jsonCreator(key, value);
                if (!componentMap.containsKey(key)){
                    componentMap.put(key, componentMap.size());
                    all_logs.add(new ArrayList<>());
                }
                all_logs.get(componentMap.get(key)).add(log);

//                System.out.println(all_logs);
//                System.out.println(componentMap);
                if (ERRORLIST.contains(log.get("status"))){
                    // ERROR alert function
                    System.out.println(log.get("status")+ "  " + key + "   " + log + "  ");
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

    private static JSONObject jsonCreator(String key, String value) {
        JSONObject log = new JSONObject();
        log.put("datetime", value.substring(0, value.indexOf(",")));
        value = value.substring(value.indexOf(",") + 1);
        log.put("logNum", value.substring(0, value.indexOf(" ")));
        value = value.substring(value.indexOf(" ") + 1);
        log.put("threadName", value.substring(0, value.indexOf(" ")));
        value = value.substring(value.indexOf(" ") + 1);
        log.put("status", value.substring(0, value.indexOf(" ")));
        value = value.substring(value.indexOf(" ") + 1);
        log.put("packageName", value.substring(0, value.indexOf(" ")));
        value = value.substring(value.indexOf(" ") + 1);
        log.put("className", value.substring(0, value.indexOf(" ")));
        value = value.substring(value.indexOf(" ") + 1);
        log.put("message", value.substring(value.indexOf("-") + 2));
        return log;

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