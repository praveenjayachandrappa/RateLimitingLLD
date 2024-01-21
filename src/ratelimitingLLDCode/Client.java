package ratelimitingLLDCode;

public class Client {
    public static void main(String[] args) {

        int limit=5;  //3 request per minute

        RateLimit rateLimit=new RateLimit(limit);

        new RateLimitHelper("praveen",rateLimit).start();

        new RateLimitHelper("kiran",rateLimit).start();

    }
}
