package ir.saleh.injester;

import ir.saleh.log.Log;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.concurrent.BlockingQueue;

/**
 * SendKafkaService get logs from Queue
 * put logs to kafka topic
 */
public class SendKafkaService extends Thread{

    private static final Logger logger = LoggerFactory.getLogger(SendKafkaService.class);
    private boolean shouldContinue;
    private final BlockingQueue<Log> passLogsQueue;
    private final String topic;
    private final Properties props;

    public SendKafkaService(BlockingQueue<Log> passLogsQueue, String topic, Properties props) {
        this.passLogsQueue = passLogsQueue;
        this.topic = topic;
        this.props = props;
        this.shouldContinue = true;
    }

    @Override
    public void run() {
        Producer<String, Log> producer = new KafkaProducer<>(props);
        while (shouldContinue || !passLogsQueue.isEmpty()){
            Log log = null;
            try {
                logger.info("read log from queue");
                log = passLogsQueue.take();
                ProducerRecord<String, Log> producerRecord = new ProducerRecord<>
                        (topic, log.getComponent(), log);
                producer.send(producerRecord);
                logger.info("log sent to kafka");
            } catch (InterruptedException e) {
                shouldContinue = false;
                logger.info("SendKafka interrupted");
            }

        }

    }
}
