package ir.saleh.injester;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.BlockingQueue;

public class WatchDirService implements Runnable {

    private final BlockingQueue<Path> passPathQueue;
    private final FileInjesterConf fileInjesterConf;

    public WatchDirService(BlockingQueue<Path> passFilesQueue, FileInjesterConf fileInjesterConf) {
        this.passPathQueue = passFilesQueue;
        this.fileInjesterConf = fileInjesterConf;
    }

    @Override
    public void run() {

        File dir = new File(fileInjesterConf.getLogPath());

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
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        WatchKey key;
        try {
            while (true) {
                key = watchService.take();
                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent<Path> pathWatchEvent = (WatchEvent<Path>) event;
                    File log = new File(dir + "/" + pathWatchEvent);
                    passPathQueue.put(log.toPath());
                }
                key.reset();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
