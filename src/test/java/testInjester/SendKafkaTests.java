package testInjester;

import ir.saleh.injester.SendKafkaService;
import ir.saleh.log.Log;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class SendKafkaTests {

    /**
     * check reading log queue
     *
     * @throws InterruptedException
     * @throws IOException
     */
    @Test
    void readLogsQueue() throws InterruptedException, IOException {
        Log log = new Log("componentTest",
                "2023-06-07 10:12:16", "thread3", "WARNING", "package.name", ".ClassName", "msg");
        BlockingQueue<Log> passLogQueue = new ArrayBlockingQueue<>(10_000);
        passLogQueue.put(log);
        Properties props = new Properties();
        InputStream inputStream = new FileInputStream("src/main/resources/configs/prop.properties");
        props.load(inputStream);
        SendKafkaService sendKafkaService = new SendKafkaService(passLogQueue, "purchases", props);
        sendKafkaService.start();
        Thread.sleep(2_000);
        Assertions.assertTrue(passLogQueue.isEmpty());
    }

    /**
     * check interrupt
     * should process data before shutdown
     *
     * @throws InterruptedException
     * @throws IOException
     */
    @Test
    void checkInterrupt() throws InterruptedException, IOException {
        Log[] log = {
                new Log("componentTest",
                        "2023-06-07 10:12:16", "thread3", "WARNING", "package.name", ".ClassName", "msg"),
                new Log("componentTest",
                        "2023-06-07 10:12:16", "thread3", "WARNING", "package.name", ".ClassName", "msg"),
                new Log("componentTest",
                        "2023-06-07 10:12:16", "thread3", "WARNING", "package.name", ".ClassName", "msg")
        };
        BlockingQueue<Log> passLogQueue = new ArrayBlockingQueue<>(10_000);
        passLogQueue.put(log[0]);
        passLogQueue.put(log[1]);
        passLogQueue.put(log[2]);
        Properties props = new Properties();
        InputStream inputStream = new FileInputStream("src/main/resources/configs/prop.properties");
        props.load(inputStream);
        SendKafkaService sendKafkaService = new SendKafkaService(passLogQueue, "purchases", props);
        sendKafkaService.start();
        sendKafkaService.interrupt();
        Thread.sleep(5_000);
        Assertions.assertTrue(passLogQueue.isEmpty());
    }
}
