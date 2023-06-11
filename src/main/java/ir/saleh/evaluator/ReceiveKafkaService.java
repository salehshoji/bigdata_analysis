package ir.saleh.evaluator;

import ir.saleh.injester.WatchDirService;
import ir.saleh.log.Log;
import org.apache.kafka.clients.consumer.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;

/**
 * ReceiveKafkaService receives logs from kafka
 * puts logs to queue
 */
public class ReceiveKafkaService extends Thread{

    private static final Logger logger = LoggerFactory.getLogger(ReceiveKafkaService.class);
    private final BlockingQueue<Log> passLogQueue;
    private final Properties props;
    private final String topic;

    public ReceiveKafkaService(BlockingQueue<Log> passLogQueue, Properties props, String topic) {
        this.passLogQueue = passLogQueue;
        this.props = props;
        this.topic = topic;
    }

    @Override
    public void run() {

        final Consumer<String, Log> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(List.of(topic));

        while (!isInterrupted()) {
            ConsumerRecords<String, Log> records = consumer.poll(Duration.ofMillis(100));
            for (ConsumerRecord<String, Log> record : records) {
                logger.info("read log from kafka");
                Log log = record.value();
                logger.debug(log.toString());
                try {
                    logger.info("put log to queue");
                    passLogQueue.put(log);
                } catch (InterruptedException e) {
                    interrupt();
                    logger.info("ReceiveKafkaService interrupted");
                }
            }
        }
    }
}
