package org.example;

import org.apache.kafka.clients.producer.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class LogProrducer {
    public static void main(String[] args) throws IOException {

        // Load producer configuration settings from a local file
        final Properties props = loadConfig("src/main/resources/getting-started.properties");
        final String topic = "purchases";
        final Producer producer = new KafkaProducer<>(props);
        File dest = new File("src/main/resources/checked_logs/");
        if (!dest.exists()){
            dest.mkdir();
        }
        File dir = new File("src/main/resources/log");

        while (true){
            File[] logs = dir.listFiles();
            if (logs.length == 0) {
                continue;
            }
            for (int i = 0; i < logs.length; i++) {
                String file_data = readfile(logs[i]);
                String log_name = logs[i].getName();
                ProducerRecord producer_record = new ProducerRecord<>(topic,log_name.substring(0, log_name.indexOf("-")), file_data);
                System.out.println(log_name.substring(0, log_name.indexOf("-")) + "    " + file_data);
                producer.send(producer_record);
            }

            //move checked files
            for (int i = 0; i < logs.length; i++) {
                logs[i].renameTo(new File("src/main/resources/checked_logs/" + logs[i].getName()));
            }
        }

//        String[] users = {"eabara", "jsmith", "sgarcia", "jbernard", "htanaka", "awalther"};
//        String[] items = {"book", "alarm clock", "t-shirts", "gift card", "batteries"};
//        try (final Producer<String, String> producer = new KafkaProducer<>(props)) {
//            final Random rnd = new Random();
//            final Long numMessages = 10L;
//
//            for (Long i = 0L; i < numMessages; i++) {
//                String user = users[rnd.nextInt(users.length)];
//                String item = items[rnd.nextInt(items.length)];
//
//                producer.send(
//                        new ProducerRecord<>(topic, user, item),
//                        (event, ex) -> {
//                            if (ex != null)
//                                ex.printStackTrace();
//                            else
//                                System.out.printf("Produced event to topic %s: key = %-10s value = %s%n", topic, user, item);
//                        });
//            }
//            System.out.printf("%s events were produced to topic %s%n", numMessages, topic);
//        }



    }

    private static String readfile(File log) throws FileNotFoundException {
        Scanner scanner = new Scanner(log);
        return scanner.nextLine();
    }

    // We'll reuse this function to load properties from the Consumer as well
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