package ir.saleh.injester;

import ir.saleh.Log;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class LogCreatorService implements Runnable {

    private final BlockingQueue<Path> passFilesQueue;
    private final BlockingQueue<Log> passLogsQueue;

    public LogCreatorService(BlockingQueue<Path> passFilesQueue, BlockingQueue<Log> passLogsQueue) {
        this.passFilesQueue = passFilesQueue;
        this.passLogsQueue = passLogsQueue;
    }


    @Override
    public void run() {
        while (true) {
            Path logFile;
            try {
                logFile = passFilesQueue.take();
                List<String> lines = Files.readString(logFile).lines().toList();
                for (String line : lines) {
                    Log log = createLog(logFile.toString().substring(logFile.toString().lastIndexOf("/") + 1,
                            logFile.toString().lastIndexOf("-")), line);
                    passLogsQueue.put(log);
                }
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

    }

    private Log createLog(String component, String logStr) {
        String[] logArray = logStr.split(" ");
        String datetime = logArray[0] + " " + logArray[1];
        String threadName = logArray[2];
        String status = logArray[3];
        String packageName = logArray[4];
        String className = logArray[5];
        String message = logArray[7];
        return new Log(component, datetime, threadName, status, packageName, className, message);
    }
}
