package ir.saleh.injester;

import ir.saleh.log.Log;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * FileInjestor main class
 * load configs (yaml, Properties)
 * runs 3 threads watchDirServiceThread, logCreatorServiceThread, sendKafkaServiceThread
 * handles interrupt using shutdownHook
 */
public class FileInjestorMain {

    public static void main(String[] args) {
        final Yaml yaml = new Yaml(new Constructor(FileInjesterConf.class));
        InputStream inputStream = FileInjestorMain.class.getClassLoader()
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

        WatchDirService watchDirServiceThread = new WatchDirService(passPathQueue, fileInjesterConf.getLogPath());
        LogCreatorService logCreatorServiceThread = new LogCreatorService(passPathQueue, passLogQueue, fileInjesterConf.getLogDestPath());
        SendKafkaService sendKafkaServiceThread = new SendKafkaService(passLogQueue, topic, props);
        watchDirServiceThread.start();
        logCreatorServiceThread.start();
        sendKafkaServiceThread.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            watchDirServiceThread.interrupt();
            logCreatorServiceThread.interrupt();
            sendKafkaServiceThread.interrupt();
        }));
    }

    /**
     * load kafka config
     *
     * @param configFile
     * @return
     * @throws IOException
     */
    private static Properties loadConfig(final String configFile) throws IOException {
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
