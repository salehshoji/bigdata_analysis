package ir.saleh.evaluator;

import ir.saleh.rest.Alert;
import ir.saleh.injester.FileInjestorMain;
import ir.saleh.log.Log;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;


/**
 * RuleEvaluator main class
 * loads configs (yaml, Properties)
 * runs 3 threads receiveKafkaServiceThread, alertCreatorServiceThread, databaseServiceThread
 * handles interrupt using shutdownHook
 */
public class RuleEvaluatorMain {
    public static void main(String[] args) throws IOException {

        final Yaml yaml = new Yaml(new Constructor(RuleEvaluatorConf.class));
        InputStream inputStream = RuleEvaluatorMain.class.getClassLoader()
                .getResourceAsStream("configs/rule-evaluator.yml");
        RuleEvaluatorConf ruleEvaluatorconf = yaml.load(inputStream);

        final String topic = ruleEvaluatorconf.getTopic();

        final Properties props = loadConfig(ruleEvaluatorconf.getKafkaPropertiesPath());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, ruleEvaluatorconf.getKafkaGroupIdConfig());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, ruleEvaluatorconf.getKafkaAutoOffsetResetConfig());
        final Consumer<String, Log> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(List.of(topic));

        BlockingQueue<Alert> passAlertQueue = new ArrayBlockingQueue<>(10_000);
        BlockingQueue<Log> passLogQueue = new ArrayBlockingQueue<>(10_000);

        ReceiveKafkaService receiveKafkaServiceThread = new ReceiveKafkaService(passLogQueue, props, topic);
        AlertCreatorService alertCreatorServiceThread = new AlertCreatorService(ruleEvaluatorconf.getDuration(), ruleEvaluatorconf.getCountLimit(),
                ruleEvaluatorconf.getRateLimit(), ruleEvaluatorconf.getErrorList(), passLogQueue, passAlertQueue);
        DatabaseService databaseServiceThread = new DatabaseService(passAlertQueue);
        receiveKafkaServiceThread.start();
        alertCreatorServiceThread.start();
        databaseServiceThread.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            receiveKafkaServiceThread.interrupt();
            alertCreatorServiceThread.interrupt();
            databaseServiceThread.interrupt();
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
