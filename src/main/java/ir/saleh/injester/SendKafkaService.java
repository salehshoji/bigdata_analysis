package ir.saleh.injester;

import ir.saleh.Log;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;
import java.util.concurrent.BlockingQueue;

public class SendKafkaService implements Runnable{
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
        Producer<String, String> producer = new KafkaProducer<>(props);
        for (Log log : passLogsQueue) {
            ProducerRecord<String, String> producerRecord = new ProducerRecord<>
                    (topic, log.toString());
            producer.send(producerRecord);

        }

    }
}
