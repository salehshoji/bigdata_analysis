package ir.saleh.injester;

import org.apache.kafka.clients.producer.*;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class FileInjester {
    public static void main(String[] args) throws IOException, InterruptedException {

        // Load FileInjester configuration settings from a yaml file
        final Yaml yaml = new Yaml(new Constructor(FileInjesterConf.class));
        InputStream inputStream = FileInjester.class.getClassLoader()
                .getResourceAsStream("configs/file-injester.yml");
        FileInjesterConf fileInjesterConf = yaml.load(inputStream);

        final Properties props = loadConfig(fileInjesterConf.getKafkaPropertiesPath());
        final String topic = fileInjesterConf.getTopic();
        final Producer<String, String> producer = new KafkaProducer<>(props);

        // open directory and create destination
        File dest = new File(fileInjesterConf.getLogDestPath());
        if (!dest.exists()) {
            dest.mkdir();
        }
        File dir = new File(fileInjesterConf.getLogPath());

        WatchService watchService = FileSystems.getDefault().newWatchService();
        Path path = Path.of(dir.getPath());
        path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);
        WatchKey key;
        while ((key = watchService.take()) != null) {
            for (WatchEvent<?> ignored : key.pollEvents()) {
                File[] logs = dir.listFiles();

                // send log to kafka
                assert logs != null;
                for (File log : logs) {
                    String fileData = Files.readString(log.toPath());
                    String logName = log.getName();
                    ProducerRecord<String, String> producerRecord = new ProducerRecord<>
                            (topic, logName.substring(0, logName.indexOf("-")), fileData);
                    producer.send(producerRecord);
                    System.out.println(logName.substring(0, logName.indexOf("-")) + "    " + fileData);
                }

                //move checked files
                for (File log : logs) {
                    log.renameTo(new File(fileInjesterConf.getLogDestPath() + log.getName()));
                }
            }
            key.reset();
        }

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