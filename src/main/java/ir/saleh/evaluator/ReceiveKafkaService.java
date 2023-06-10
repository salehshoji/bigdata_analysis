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
    private String topic;

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
                String componentname = record.key();
                Log log = record.value();
                System.out.println(log);

//                for (String line : value.lines().toList()) {
//                    Log log = logCreator(key, line);
//                    if (!componentMap.containsKey(key)) {
//                        componentMap.put(key, new ArrayList<>());
//                    }
//                    List<Log> logList = componentMap.get(key);
//                    logList.add(log);
//                    checkLogType(key, log);
//                    while (ChronoUnit.SECONDS.between(logList.get(0).getDateTime(), log.getDateTime()) > DURATION) {
//                        logList.remove(0);
//                    }
//                    checkComponentProblems(logList, key);
//                }
            }
        }
    }
}
