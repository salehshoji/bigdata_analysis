package ir.saleh.injester;

import ir.saleh.log.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class LogCreatorService extends Thread{

    private static final Logger logger = LoggerFactory.getLogger(LogCreatorService.class);
    private final BlockingQueue<Path> passPathQueue;
    private final BlockingQueue<Log> passLogsQueue;
    private String logDestPath;

    public LogCreatorService(BlockingQueue<Path> passPathsQueue, BlockingQueue<Log> passLogsQueue, String logDestPath) {
        this.passPathQueue = passPathsQueue;
        this.passLogsQueue = passLogsQueue;
        this.logDestPath = logDestPath;
    }


    @Override
    public void run() {
        File dest = new File(logDestPath);
        if (!dest.exists()) {
            dest.mkdir();
        }
        while (!isInterrupted() || !passPathQueue.isEmpty()) {
            Path logFile;
            try {
                logFile = passPathQueue.take();
                List<String> lines = Files.readString(logFile).lines().toList();
                for (String line : lines) {
                    Log log = createLog(logFile.toString().substring(logFile.toString().lastIndexOf("/") + 1,
                            logFile.toString().lastIndexOf("-")), line);
                    passLogsQueue.put(log);
                }
                logFile.toFile().renameTo(new File(logDestPath + logFile.getFileName()));
            } catch (InterruptedException e) {
                interrupt();
                logger.info("LogCreator interrupted");
            }catch (IOException e){
                throw new RuntimeException(e);
            }
        }

    }

    private Log createLog(String component, String logStr) {
        String[] logArray = logStr.split(" ");
        String datetime = logArray[0] + " " + logArray[1].substring(0, logArray[1].indexOf(','));
        String threadName = logArray[2];
        String status = logArray[3];
        String packageName = logArray[4];
        String className = logArray[5];
        String message = logArray[7];
        return new Log(component, datetime, threadName, status, packageName, className, message);
    }
}
