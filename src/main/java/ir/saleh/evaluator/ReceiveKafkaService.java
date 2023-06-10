package ir.saleh.evaluator;

import ir.saleh.log.Log;
import org.apache.kafka.clients.consumer.*;

import java.time.Duration;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;

public class ReceiveKafkaService implements Runnable{
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

        while (true) {
            ConsumerRecords<String, Log> records = consumer.poll(Duration.ofMillis(100));
            for (ConsumerRecord<String, Log> record : records) {
                Log log = record.value();
                System.out.println(log);
                try {
                    passLogQueue.put(log);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
