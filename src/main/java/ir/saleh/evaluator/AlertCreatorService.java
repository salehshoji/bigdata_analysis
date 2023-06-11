package ir.saleh.evaluator;

import ir.saleh.rest.Alert;
import ir.saleh.log.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

/**
 * AlertCreatorService gets logs from queue
 * processes logs and creates alerts
 * puts alerts to queue
 */
public class AlertCreatorService extends Thread {
    private boolean shouldContinue;
    private static final Logger logger = LoggerFactory.getLogger(AlertCreatorService.class);
    private final float duration; // second
    private final float countLimit; // number in TIME_LIMIT
    private final float rateLimit; // log per second
    private final List<String> errorList;
    private final BlockingQueue<Log> passLogQueue;
    private final BlockingQueue<Alert> passAlertQueue;

    private final Map<String, List<Log>> componentMap;


    public AlertCreatorService(float duration, float countLimit, float rateLimit, List<String> errorList,
                               BlockingQueue<Log> passLogQueue, BlockingQueue<Alert> passAlertQueue) {
        this.duration = duration;
        this.countLimit = countLimit;
        this.rateLimit = rateLimit;
        this.errorList = errorList;
        this.passLogQueue = passLogQueue;
        this.passAlertQueue = passAlertQueue;
        this.componentMap = new HashMap<>();
        this.shouldContinue = true;
    }

    @Override
    public void run() {
        while (shouldContinue || !passLogQueue.isEmpty()) {
            try {
                logger.info("read log from queue");
                Log log = passLogQueue.take();
                if (!componentMap.containsKey(log.getComponent())) {
                    logger.info("put alert to queue");
                    componentMap.put(log.getComponent(), new ArrayList<>());
                }
                List<Log> logList = componentMap.get(log.getComponent());
                logList.add(log);
                checkLogType(log);
                while (ChronoUnit.MINUTES.between(logList.get(0).getDateTime(), log.getDateTime()) > duration) {
                    logList.remove(0);
                }
                checkCountLimit(logList);
                checkRate(logList, log.getComponent());

            } catch (InterruptedException e) {
                shouldContinue = false;
                logger.info("AlertCreatorService interrupted");
            }
        }
    }

    /**
     * checks first rule for input log
     *
     * @param log
     * @throws InterruptedException
     */
    public void checkLogType(Log log) throws InterruptedException {
        if (errorList.contains(log.getStatus())) {
            // ERROR alert function
            Alert alert = new Alert(log.getComponent(), "first_rule ",
                    "rule1 " + log.getComponent() + log.getStatus() + "  " + "   " + log.getMessage() + " on " + log.getDateTime());
            passAlertQueue.put(alert);
        }
    }

    /**
     * checks second rules for input log list
     *
     * @param logList
     * @throws InterruptedException
     */
    public void checkCountLimit(List<Log> logList) throws InterruptedException {
        // rule 2
        LocalDateTime startTime = null;
        int startIndex = 0;
        for (int i = 0; i < logList.size(); i++) {
            if (startTime == null) {
                startTime = logList.get(i).getDateTime();
            } else {
                while (ChronoUnit.MINUTES.between(startTime, logList.get(i).getDateTime()) > duration) {
                    startIndex += 1;
                    startTime = logList.get(startIndex).getDateTime();
                }
                if (i - startIndex > countLimit) {
                    Alert alert = new Alert(logList.get(0).getComponent(), "second_alert",
                            ("rule2 in component" + logList.get(0).getComponent() + "from " + startTime + " to "
                                    + logList.get(i).getDateTime() + " we have " + (i - startIndex) + "error"));
                    passAlertQueue.put(alert);
                }
            }
        }
    }

    /**
     * checks third rule for input log list
     *
     * @param logList
     * @param component
     * @throws InterruptedException
     */
    public void checkRate(List<Log> logList, String component) throws InterruptedException {
        // rule 3
        if ((ChronoUnit.MINUTES.between(logList.get(0).getDateTime(), logList.get(logList.size() - 1).getDateTime()) == 0)) {
            if (logList.size() > rateLimit) {
                new Alert(component, "third_rule",
                        "rule 3 in component" + component + " rate is "
                                + (logList.size()) + "in less than second!!!" + " and its more than " + rateLimit);
            }
        } else if ((float) logList.size() / (ChronoUnit.MINUTES.between(logList.get(0).getDateTime(), logList.get(logList.size() - 1).getDateTime())) > rateLimit) {
            Alert alert = new Alert(component, "third_rule",
                    "rule 3 in component" + component + " rate is "
                            + (logList.size() / (ChronoUnit.MINUTES.between(logList.get(0).getDateTime(), logList.get(logList.size() - 1).getDateTime())))
                            + " and its more than " + rateLimit);
            passAlertQueue.put(alert);
        }
    }
}
