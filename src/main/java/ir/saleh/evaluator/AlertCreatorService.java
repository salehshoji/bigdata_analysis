package ir.saleh.evaluator;

import ir.saleh.Alert;
import ir.saleh.log.Log;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

public class AlertCreatorService implements Runnable{

    private final float duration; // second
    private final float countLimit; // number in TIME_LIMIT
    private final float rateLimit; // log per second
    private final List<String> errorList;
    private final BlockingQueue<Log> passLogQueue;
    private final BlockingQueue<Log> passAlertQueue;

    private final Map<String, List<Log>> componentMap;


    private static float DURATION;


    public AlertCreatorService(float duration, float countLimit, float rateLimit, List<String> errorList, BlockingQueue<Log> passLogQueue, BlockingQueue<Log> passAlertQueue) {
        this.duration = duration;
        this.countLimit = countLimit;
        this.rateLimit = rateLimit;
        this.errorList = errorList;
        this.passLogQueue = passLogQueue;
        this.passAlertQueue = passAlertQueue;
        this.componentMap = new HashMap<>();
    }

    @Override
    public void run() {
        while (true) {
            try {
                Log log = passLogQueue.take();
                if (!componentMap.containsKey(log.getComponent())) {
                        componentMap.put(log.getComponent(), new ArrayList<>());
                    }
                    List<Log> logList = componentMap.get(log.getComponent());
                    logList.add(log);
                    checkLogType(log.getComponent(), log);
                    while (ChronoUnit.SECONDS.between(logList.get(0).getDateTime(), log.getDateTime()) > DURATION) {
                        logList.remove(0);
                    }
                    checkComponentProblems(logList, log.getComponent());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }


    private void checkLogType(String key, Log log) {
        if (errorList.contains(log.getStatus())) {
            // ERROR alert function
            new Alert(key, "first_rule",
                    "rule1" + log.getStatus() + "  " + key + "   " + log + " on " + log.getDateTime());

        }
    }

    private void checkComponentProblems(List<Log> logList, String component) {
        // rule 2
        LocalDateTime startTime = null;
        int startIndex = 0;
        for (int i = 0; i < logList.size(); i++) {
            if (startTime == null) {
                startTime = logList.get(i).getDateTime();
            } else {
                while (ChronoUnit.SECONDS.between(startTime, logList.get(i).getDateTime()) > DURATION) {
                    startIndex += 1;
                    startTime = logList.get(startIndex).getDateTime();
                }
                if (i - startIndex > countLimit) {
                    new Alert(component, "second_alert",
                            ("rule2 in component" + component + "from " + startTime + " to "
                                    + logList.get(i).getDateTime() + " we have " + (i - startIndex) + "error"));
                }
            }
        }

        // rule 3
        if ((ChronoUnit.SECONDS.between(logList.get(0).getDateTime(), logList.get(logList.size() - 1).getDateTime()) == 0)) {
            if (logList.size() > rateLimit) {
                new Alert(component, "third_rule",
                        "rule 3 in component" + component + " rate is "
                                + (logList.size()) + "in less than second!!!" + " and its more than " + rateLimit);
            }
        } else if (logList.size() / (ChronoUnit.SECONDS.between(logList.get(0).getDateTime(), logList.get(logList.size() - 1).getDateTime())) > rateLimit) {
            new Alert(component, "third_rule",
                    "rule 3 in component" + component + " rate is "
                            + (logList.size() / (ChronoUnit.SECONDS.between(logList.get(0).getDateTime(), logList.get(logList.size() - 1).getDateTime())))
                            + " and its more than " + rateLimit);
        }

    }
}
