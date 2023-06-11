package evaluator;

import ir.saleh.evaluator.DatabaseService;
import ir.saleh.injester.SendKafkaService;
import ir.saleh.log.Log;
import ir.saleh.rest.Alert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class DatabaseServiceTests {

    /**
     * check reading alert queue
     *
     * @throws InterruptedException
     * @throws IOException
     */
    @Test
    void readAlertsQueue() throws InterruptedException, IOException {
        BlockingQueue<Alert> passAlertQueue = new ArrayBlockingQueue<>(10_000);
        Alert alert = new Alert("componentTest", "first_rule",
                "rule1 componentTestWARNING     msg on 2023-06-07T10:12:16");
        passAlertQueue.put(alert);
        DatabaseService databaseService = new DatabaseService(passAlertQueue);
        databaseService.start();
        Thread.sleep(2_000);
        Assertions.assertTrue(passAlertQueue.isEmpty());
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
        Alert[] alerts = {
                new Alert("componentTest", "first_rule",
                        "rule1 componentTestWARNING     msg on 2023-06-07T10:12:16"),
                new Alert("componentTest", "first_rule",
                        "rule1 componentTestWARNING     msg on 2023-06-07T10:12:16"),
                new Alert("componentTest", "first_rule",
                        "rule1 componentTestWARNING     msg on 2023-06-07T10:12:16"),
        };

        BlockingQueue<Alert> passAlertQueue = new ArrayBlockingQueue<>(10_000);
        passAlertQueue.put(alerts[0]);
        passAlertQueue.put(alerts[1]);
        passAlertQueue.put(alerts[2]);
        DatabaseService databaseService = new DatabaseService(passAlertQueue);
        databaseService.start();
        databaseService.interrupt();
        Thread.sleep(5_000);
        Assertions.assertTrue(passAlertQueue.isEmpty());
    }
}
