package ir.saleh.evaluator;

import java.util.List;

public class AlertCreatorService implements Runnable{

    private final float duration; // second
    private final float countLimit; // number in TIME_LIMIT
    private final float rateLimit; // log per second
    private static List<String> ERROR_LIST;

    public AlertCreatorService(float duration, float countLimit, float rateLimit) {
        this.duration = duration;
        this.countLimit = countLimit;
        this.rateLimit = rateLimit;
    }

    @Override
    public void run() {

    }
}
