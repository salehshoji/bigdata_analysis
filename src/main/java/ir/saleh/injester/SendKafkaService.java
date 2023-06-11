package ir.saleh.injester;

import ir.saleh.log.Log;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.concurrent.BlockingQueue;

public class SendKafkaService extends Thread{

    private static final Logger logger = LoggerFactory.getLogger(SendKafkaService.class);
    private final BlockingQueue<Log> passLogsQueue;
    private final String topic;
    private final Properties props;

    public SendKafkaService(BlockingQueue<Log> passLogsQueue, String topic, Properties props) {
        this.passLogsQueue = passLogsQueue;
        this.topic = topic;
        this.props = props;
    }

    @Override
    public void run() {
        Producer<String, Log> producer = new KafkaProducer<>(props);
        while (!isInterrupted() || !passLogsQueue.isEmpty()){
            Log log = null;
            try {
                log = passLogsQueue.take();
                ProducerRecord<String, Log> producerRecord = new ProducerRecord<>
                        (topic, log.getComponent(), log);
                producer.send(producerRecord);
            } catch (InterruptedException e) {
                interrupt();
                logger.info("SendKafka interrupted");
            }

        }

    }
}
