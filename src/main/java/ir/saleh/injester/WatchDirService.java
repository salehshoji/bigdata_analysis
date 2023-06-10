package ir.saleh.injester;

import org.apache.kafka.clients.producer.ProducerRecord;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class WatchDirService implements Runnable{

    private final BlockingQueue<File> passFilesQueue;
    private final FileInjesterConf fileInjesterConf;

    public WatchDirService(BlockingQueue<File> passFilesQueue, FileInjesterConf fileInjesterConf) {
        this.passFilesQueue = passFilesQueue;
        this.fileInjesterConf = fileInjesterConf;
    }

    @Override
    public void run() {

        // open directory and create destination
        File dest = new File(fileInjesterConf.getLogDestPath());
        if (!dest.exists()) {
            dest.mkdir();
        }
        File dir = new File(fileInjesterConf.getLogPath());

        WatchService watchService = null;
        try {
            watchService = FileSystems.getDefault().newWatchService();
            Path path = Path.of(dir.getPath());
            path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        WatchKey key;
        File[] logs = dir.listFiles();
        assert logs != null;
        passFilesQueue.addAll(Arrays.asList(logs));
        while (true) {
            try {
                if ((key = watchService.take()) == null) break;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent<Path> pathWatchEvent = (WatchEvent<Path>)event;
                File log = new File(dir + "/" + pathWatchEvent);
                passFilesQueue.add(log);
                }
            }
            key.reset();
    }
}
