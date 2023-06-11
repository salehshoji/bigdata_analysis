package testInjester;

import ir.saleh.injester.LogCreatorService;
import ir.saleh.log.Log;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Path;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class LogCreatorTests {
    /**
     * check assigning log attributes
     */
    @Test
    void createLogTest() {
        BlockingQueue<Path> passPathQueue = new ArrayBlockingQueue<>(10_000);
        BlockingQueue<Log> passLogQueue = new ArrayBlockingQueue<>(10_000);
        LogCreatorService logCreatorService = new LogCreatorService(passPathQueue, passLogQueue,
                "src/test/resources/checked_logs/");
        Log log = logCreatorService.createLog("componentTest",
                "2023-06-07 10:12:16,113 thread3 WARNING package.name .ClassName - msg");
        Assertions.assertEquals("componentTest", log.getComponent());
        Assertions.assertEquals("WARNING", log.getStatus());
        Assertions.assertEquals("2023-06-07T10:12:16", log.getDateTime().toString());
        Assertions.assertEquals("msg", log.getMessage());
        Assertions.assertEquals(".ClassName", log.getClassname());
        Assertions.assertEquals("package.name", log.getPackageName());
        Assertions.assertEquals("thread3", log.getThreadName());
    }

    /**
     * check queueing logs from log file in passPathQueue
     *
     * @throws InterruptedException
     */
    @Test
    void processLogFile() throws InterruptedException {
        new File("src/test/resources/checked_logs/comp1-2023_06_07-10_12_16.log").renameTo
                (new File("src/test/resources/log/comp1-2023_06_07-10_12_16.log"));
        BlockingQueue<Path> passPathQueue = new ArrayBlockingQueue<>(10_000);
        BlockingQueue<Log> passLogQueue = new ArrayBlockingQueue<>(10_000);
        LogCreatorService logCreatorService = new LogCreatorService(passPathQueue, passLogQueue,
                "src/test/resources/checked_logs/");
        passPathQueue.put(Path.of("src/test/resources/log/comp1-2023_06_07-10_12_16.log"));
        logCreatorService.start();
        Thread.sleep(2_000);
        Assertions.assertTrue(passPathQueue.isEmpty());
        Assertions.assertEquals(7, passLogQueue.size());
        new File("src/test/resources/checked_logs/comp1-2023_06_07-10_12_16.log").renameTo
                (new File("src/test/resources/log/comp1-2023_06_07-10_12_16.log"));
    }

    @Test
    void interruptTest() throws InterruptedException {
        new File("src/test/resources/checked_logs/comp1-2023_06_07-10_12_16.log").renameTo
                (new File("src/test/resources/log/comp1-2023_06_07-10_12_16.log"));
        new File("src/test/resources/checked_logs/comp1-2023_06_07-10_12_21.log").renameTo
                (new File("src/test/resources/log/comp1-2023_06_07-10_12_21.log"));
        new File("src/test/resources/checked_logs/comp2-2023_06_07-10_12_23.log").renameTo
                (new File("src/test/resources/log/comp2-2023_06_07-10_12_23.log"));
        File[] files = {
                new File("src/test/resources/log/comp1-2023_06_07-10_12_16.log"),

                new File("src/test/resources/log/comp1-2023_06_07-10_12_21.log"),

                new File("src/test/resources/log/comp2-2023_06_07-10_12_23.log")
        };
        BlockingQueue<Path> passPathQueue = new ArrayBlockingQueue<>(10_000);
        passPathQueue.put(files[0].toPath());
        passPathQueue.put(files[1].toPath());
        passPathQueue.put(files[2].toPath());
        BlockingQueue<Log> passLogQueue = new ArrayBlockingQueue<>(10_000);
        LogCreatorService logCreatorService = new LogCreatorService(passPathQueue, passLogQueue,
                "src/test/resources/checked_logs/");
        logCreatorService.start();
        logCreatorService.interrupt();
        Thread.sleep(2_000);
        Assertions.assertTrue(passPathQueue.isEmpty());
        Assertions.assertEquals(9, passLogQueue.size());

    }
}
