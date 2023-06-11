package ir.saleh.injester;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;

/**
 * WatchDirService watch logs directory
 * put log files path to passPathQueue
 */
public class WatchDirService extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(WatchDirService.class);

    private final BlockingQueue<Path> passPathQueue;
    private final String logPath;

    public WatchDirService(BlockingQueue<Path> passPathsQueue, String logPath) {
        this.passPathQueue = passPathsQueue;
        this.logPath = logPath;
    }

    @Override
    public void run() {
        File dir = new File(logPath);
        logger.info("WatchDirService starting...");
        WatchService watchService = null;
        try {
            watchService = FileSystems.getDefault().newWatchService();
            Path path = Path.of(dir.getPath());
            path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        File[] logs = dir.listFiles();
        assert logs != null;
        for (File log : logs) {
            try {
                passPathQueue.put(log.toPath());
                logger.info("put file to queue");
                logger.debug("put file to queue");
            } catch (InterruptedException e) {
                this.interrupt();
                logger.info("WatchDirService interrupted");
            }
        }

        WatchKey key;
        try {
            while (!isInterrupted()) {
                key = watchService.take();
                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent<Path> pathWatchEvent = (WatchEvent<Path>) event;
                    File log = new File(dir + "/" + pathWatchEvent.context());
                    logger.debug("put file to queue");
                    passPathQueue.put(log.toPath());
                }
                key.reset();
            }
        } catch (InterruptedException e) {
            this.interrupt();
            logger.info("WatchDirService interrupted");
        }
    }
}
