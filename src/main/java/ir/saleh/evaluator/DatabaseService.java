package ir.saleh.evaluator;

import ir.saleh.rest.Alert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.concurrent.BlockingQueue;

/**
 * reads alerts from queue
 * saves alerts to database
 */
public class DatabaseService extends Thread {
    private boolean shouldContinue;

    private static final Logger logger = LoggerFactory.getLogger(AlertCreatorService.class);

    private final BlockingQueue<Alert> passAlertQueue;

    public DatabaseService(BlockingQueue<Alert> passAlertQueue) {
        this.passAlertQueue = passAlertQueue;
        this.shouldContinue = true;
    }

    @Override
    public void run() {
        while (shouldContinue || !passAlertQueue.isEmpty()) {
            try {
                logger.info("read alert from queue");
                passAlertQueue.take().pushToDatabase();
                logger.info("put alert to database");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                shouldContinue = false;
                logger.info("AlertCreatorService interrupted");
            }
        }
    }
}
