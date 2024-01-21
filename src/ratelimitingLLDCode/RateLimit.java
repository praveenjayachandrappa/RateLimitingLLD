package ratelimitingLLDCode;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * /**
 *  * Implementation of Sliding window algorithm with timestamp and counter
 *  * (example: redis hash)
 *  */

public class RateLimit {

    private int rateLimit;

    Map<String, LinkedList<Request>> userRequestMap;


    public RateLimit(int rateLimit) {
        this.rateLimit = rateLimit;
        userRequestMap= new ConcurrentHashMap<>();
    }

    /**
     * Thread safe block being invoked by multiple threads
     * @param user username
     * @param timestamp timestamp of request
     * @return request allowed true/false
     */

    public synchronized  boolean hit(String user, Instant timestamp){

        if(!userRequestMap.containsKey(user)){
            return addNewUser(user);
        }
        else{
            if(getTotalElpasedRequests(user) < rateLimit){
                LinkedList<Request> requests = userRequestMap.get(user);
                requests.add(new Request(timestamp,1));
                userRequestMap.put(user,requests);
                return true;
            }
            else {

                /***
                 *
                 * Rate limiting allowing 3 requests per minute for User1
                 *
                 * [Allow request] 7:00:00 AM  ---- "User1": {1574860100: 1}
                 * [Allow request] 7:01:05 AM  ---- "User1": { 1574860100: 1, 1574860160: 1}
                 * [Allow request] 7:01:20 AM  ---- "User1": { 1574860100: 1, 1574860160: 2}
                 * [Allow request] 7:01:20 AM  ---- "User1": { 1574860100: 1, 1574860160: 3}
                 * [Reject request] 7:01:45 AM ---- "User1": { 1574860100: 1, 1574860160: 3}
                 * [Allow request] 7:02:20 AM  ---- "User1": { 1574860160: 3, 1574860220: 1}
                 */

                boolean actionTaken=false;
                for (int i = 0; i < userRequestMap.get(user).size(); i++) {
                    Duration duration=Duration.between(userRequestMap.get(user).get(i).getTimestamp(),timestamp);
                    if(duration.getSeconds() >= 60){
                        userRequestMap.get(user).remove(i);
                        actionTaken=true;
                    }
                    else {
                        break;
                    }
                }
                if(actionTaken){
                    LinkedList<Request> requests = userRequestMap.get(user);
                    requests.add(new Request(timestamp,1));
                    userRequestMap.put(user,requests);
                    return true;
                }
            }
        }
        return false;
    }

    private Integer getTotalElpasedRequests(String user) {
        return userRequestMap.get(user).stream().mapToInt(Request::getCount).sum();
    }

    private boolean addNewUser(String user) {
        LinkedList<Request> requests = new LinkedList<>();
        requests.add(new Request(Instant.now(),1));
        userRequestMap.put(user,requests);
        System.out.println("New User added !! " + user);
        return true;
    }

}
