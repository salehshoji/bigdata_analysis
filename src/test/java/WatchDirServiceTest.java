import ir.saleh.injester.WatchDirService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;


public class WatchDirServiceTest {
    /**
     * checks adding existing files to Queue
     * @throws InterruptedException
     */
    @Test
    void checkAddingExistingFile() throws InterruptedException {
        BlockingQueue<Path> passPathQueue = new ArrayBlockingQueue<>(10_000);
        WatchDirService watchDirService = new WatchDirService(passPathQueue, "src/test/resources/log");
        watchDirService.start();
        Thread.sleep(2_000);
        watchDirService.interrupt();
        Assertions.assertFalse(passPathQueue.isEmpty());
        Assertions.assertEquals(passPathQueue.size(), 6);
    }

    @Test
    void checkAddingNewFile() throws InterruptedException {
        BlockingQueue<Path> passPathQueue = new ArrayBlockingQueue<>(10_000);
        WatchDirService watchDirService = new WatchDirService(passPathQueue, "src/test/resources/log");
        File logFile = Objects.requireNonNull(new File("src/test/resources/logSource").listFiles())[0];
        watchDirService.start();
        File newfile;
        logFile.renameTo(newfile = new File("src/test/resources/log/" + logFile.getName()));
        Thread.sleep(2_000);
        watchDirService.interrupt();
        Assertions.assertFalse(passPathQueue.isEmpty());
        Assertions.assertEquals(passPathQueue.size(), 7);
        newfile.renameTo(new File("src/test/resources/logSource/" + logFile.getName()));
    }


}


