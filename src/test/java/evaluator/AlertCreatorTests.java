package evaluator;

import ir.saleh.evaluator.AlertCreatorService;
import ir.saleh.injester.LogCreatorService;
import ir.saleh.log.Log;
import ir.saleh.rest.Alert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class AlertCreatorTests {
    /**
     * check assigning alert attributes
     */
    @Test
    void createAlertTest() {
        BlockingQueue<Log> passLogQueue = new ArrayBlockingQueue<>(10_000);
        BlockingQueue<Alert> passAlertQueue = new ArrayBlockingQueue<>(10_000);
        AlertCreatorService alertCreatorService = new AlertCreatorService
                (1, 1, 1, List.of("ERROR", "WARNING"), passLogQueue, passAlertQueue);
        Alert alert = new Alert("componentTest", "first_rule",
                "rule1 componentTestWARNING     msg on 2023-06-07T10:12:16");
        Assertions.assertEquals("componentTest", alert.getComponentName());
        Assertions.assertEquals("first_rule", alert.getAlertName());
        Assertions.assertEquals("rule1 componentTestWARNING     msg on 2023-06-07T10:12:16", alert.getDescription());
        Assertions.assertEquals(Alert.alertsList.size() - 1, alert.getId());
    }

    /**
     * check rule 1 alert
     *
     * @throws InterruptedException
     */
    @Test
    void checkRule1() throws InterruptedException {
        BlockingQueue<Log> passLogQueue = new ArrayBlockingQueue<>(10_000);
        BlockingQueue<Alert> passAlertQueue = new ArrayBlockingQueue<>(10_000);
        Log log = new Log("componentTest",
                "2023-06-07 10:12:16", "thread3", "WARNING", "package.name", ".ClassName", "msg");
        Log log1 = new Log("componentTest",
                "2023-06-07 10:12:16", "thread3", "INFO", "package.name", ".ClassName", "msg");
        AlertCreatorService alertCreatorService = new AlertCreatorService
                (1, 1, 1, List.of("ERROR", "WARNING"), passLogQueue, passAlertQueue);
        alertCreatorService.checkLogType(log);
        alertCreatorService.checkLogType(log1);
        Assertions.assertEquals(1, passAlertQueue.size());
    }

    /**
     * check rule 3 alert
     *
     * @throws InterruptedException
     */
    @Test
    void checkRule2() throws InterruptedException {
        BlockingQueue<Log> passLogQueue = new ArrayBlockingQueue<>(10_000);
        BlockingQueue<Alert> passAlertQueue = new ArrayBlockingQueue<>(10_000);
        Log log = new Log("componentTest",
                "2023-06-07 10:12:16", "thread3", "WARNING", "package.name", ".ClassName", "msg");
        Log log1 = new Log("componentTest",
                "2023-06-07 10:13:16", "thread3", "INFO", "package.name", ".ClassName", "msg");
        Log log2 = new Log("componentTest",
                "2023-06-07 10:14:16", "thread3", "INFO", "package.name", ".ClassName", "msg");
        Log log3 = new Log("componentTest",
                "2023-06-07 10:15:16", "thread3", "INFO", "package.name", ".ClassName", "msg");
        Log log4 = new Log("componentTest",
                "2023-06-07 10:16:16", "thread3", "INFO", "package.name", ".ClassName", "msg");
        Log log5 = new Log("componentTest",
                "2023-06-07 10:17:16", "thread3", "INFO", "package.name", ".ClassName", "msg");
        AlertCreatorService alertCreatorService = new AlertCreatorService
                (10, 4, 1, List.of("ERROR", "WARNING"), passLogQueue, passAlertQueue);
        alertCreatorService.checkCountLimit(List.of(log, log1, log2, log3, log4, log5));
        Assertions.assertEquals(1, passAlertQueue.size());
    }

    /**
     * check rule 3 alert
     *
     * @throws InterruptedException
     */
    @Test
    void checkRule3() throws InterruptedException {
        BlockingQueue<Log> passLogQueue = new ArrayBlockingQueue<>(10_000);
        BlockingQueue<Alert> passAlertQueue = new ArrayBlockingQueue<>(10_000);
        Log log = new Log("componentTest",
                "2023-06-07 10:12:16", "thread3", "WARNING", "package.name", ".ClassName", "msg");
        Log log1 = new Log("componentTest",
                "2023-06-07 10:13:16", "thread3", "INFO", "package.name", ".ClassName", "msg");
        Log log2 = new Log("componentTest",
                "2023-06-07 10:14:16", "thread3", "INFO", "package.name", ".ClassName", "msg");
        Log log3 = new Log("componentTest",
                "2023-06-07 10:15:16", "thread3", "INFO", "package.name", ".ClassName", "msg");
        Log log4 = new Log("componentTest",
                "2023-06-07 10:16:16", "thread3", "INFO", "package.name", ".ClassName", "msg");
        Log log5 = new Log("componentTest",
                "2023-06-07 10:17:16", "thread3", "INFO", "package.name", ".ClassName", "msg");
        AlertCreatorService alertCreatorService = new AlertCreatorService
                (10, 4, 2, List.of("ERROR", "WARNING"), passLogQueue, passAlertQueue);
        alertCreatorService.checkCountLimit(List.of(log, log1, log2, log3, log4, log5));
        Assertions.assertEquals(1, passAlertQueue.size());
    }

}
