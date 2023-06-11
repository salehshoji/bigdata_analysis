package testInjester;

import ir.saleh.injester.WatchDirService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;


public class WatchDirServiceTests {
    /**
     * checks adding existing files to Queue
     *
     * @throws InterruptedException
     */
    @Test
    void checkAddingExistingFile() throws InterruptedException {
        new File("src/test/resources/checked_logs/comp1-2023_06_07-10_12_16.log").renameTo
                (new File("src/test/resources/log/comp1-2023_06_07-10_12_16.log"));
        new File("src/test/resources/checked_logs/comp1-2023_06_07-10_12_21.log").renameTo
                (new File("src/test/resources/log/comp1-2023_06_07-10_12_21.log"));
        new File("src/test/resources/checked_logs/comp2-2023_06_07-10_12_23.log").renameTo
                (new File("src/test/resources/log/comp2-2023_06_07-10_12_23.log"));
        BlockingQueue<Path> passPathQueue = new ArrayBlockingQueue<>(10_000);
        WatchDirService watchDirService = new WatchDirService(passPathQueue, "src/test/resources/log");
        watchDirService.start();
        Thread.sleep(2_000);
        watchDirService.interrupt();
        Assertions.assertFalse(passPathQueue.isEmpty());
        Assertions.assertEquals(3, passPathQueue.size());

        new File("src/test/resources/log/comp1-2023_06_07-10_12_16.log").renameTo
                (new File("src/test/resources/checked_logs/comp1-2023_06_07-10_12_16.log"));
        new File("src/test/resources/log/comp1-2023_06_07-10_12_21.log").renameTo
                (new File("src/test/resources/checked_logs/comp1-2023_06_07-10_12_21.log"));
        new File("src/test/resources/log/comp2-2023_06_07-10_12_23.log").renameTo
                (new File("src/test/resources/checked_logs/comp2-2023_06_07-10_12_23.log"));
    }

    /**
     * checks adding new files to Queue
     *
     * @throws InterruptedException
     */
    @Test
    void checkAddingNewFile() throws InterruptedException {
        BlockingQueue<Path> passPathQueue = new ArrayBlockingQueue<>(10_000);
        WatchDirService watchDirService = new WatchDirService(passPathQueue, "src/test/resources/log");
        File logFile = Objects.requireNonNull(new File("src/test/resources/checked_logs").listFiles())[0];
        watchDirService.start();
        File newfile;
        logFile.renameTo(newfile = new File("src/test/resources/log/" + logFile.getName()));
        Thread.sleep(2_000);
        watchDirService.interrupt();
        Assertions.assertFalse(passPathQueue.isEmpty());
        Assertions.assertEquals(passPathQueue.size(), 1);
        newfile.renameTo(new File("src/test/resources/logSource/" + logFile.getName()));
    }


}


