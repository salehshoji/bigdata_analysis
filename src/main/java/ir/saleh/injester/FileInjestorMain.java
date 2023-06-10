package ir.saleh.injester;

import ir.saleh.Log;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class FileInjestorMain {
    public static void main(String[] args) {
        final Yaml yaml = new Yaml(new Constructor(FileInjesterConf.class));
        InputStream inputStream = FileInjester.class.getClassLoader()
                .getResourceAsStream("configs/file-injester.yml");
        FileInjesterConf fileInjesterConf = yaml.load(inputStream);

        final Properties props;
        try {
            props = loadConfig(fileInjesterConf.getKafkaPropertiesPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        final String topic = fileInjesterConf.getTopic();
        BlockingQueue<Path> passPathQueue = new ArrayBlockingQueue<>(10_000);
        BlockingQueue<Log> passLogQueue = new ArrayBlockingQueue<>(10_000);


        WatchDirService watchDirService = new WatchDirService(passPathQueue, fileInjesterConf.getLogPath());
        Thread watchDirServiceThread = new Thread(watchDirService);
        LogCreatorService logCreatorService = new LogCreatorService(passPathQueue, passLogQueue, fileInjesterConf.getLogDestPath());
        Thread logCreatorServiceThread = new Thread(logCreatorService);
        SendKafkaService sendKafkaService = new SendKafkaService(passLogQueue, topic, props);
        Thread sendKafkaServiceThread = new Thread(sendKafkaService);
        watchDirServiceThread.start();
        logCreatorServiceThread.start();
        sendKafkaServiceThread.start();
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
