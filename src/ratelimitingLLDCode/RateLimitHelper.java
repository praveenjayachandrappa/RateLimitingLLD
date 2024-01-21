package ratelimitingLLDCode;

import java.time.Instant;

/**
 * Thread Helper class - Invoke multiple threads to check throttling of requests
 */
public class RateLimitHelper extends Thread {

    RateLimit rateLimit;

    public RateLimitHelper(String name, RateLimit rateLimit) {
        super(name);
        this.rateLimit = rateLimit;
    }

    @Override
    public void run() {

        for (int i = 0; i <= 65; i++) {
            System.out.println("Thread Name - " + getName() + ", Time - " + i + "," +
                    " rate limit: " + hit(getName(), Instant.now()));
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("DONE! " + getName());
    }

    private boolean hit(String name, Instant now) {
        return rateLimit.hit(name, now);
    }


}
