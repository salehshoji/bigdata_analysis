package ir.saleh.evaluator;

import ir.saleh.Alert;
import ir.saleh.log.Log;

import java.sql.SQLException;
import java.util.concurrent.BlockingQueue;

public class DatabaseService extends Thread{

    private final BlockingQueue<Alert> passAlertQueue;

    public DatabaseService(BlockingQueue<Alert> passAlertQueue) {
        this.passAlertQueue = passAlertQueue;
    }

    @Override
    public void run() {
        while (true) {
            try {
                passAlertQueue.take().pushToDatabase();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
