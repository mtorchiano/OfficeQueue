package it.polito.oop.office;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.*;
import static java.util.Comparator.*;

/**
 * Manages a waiting queue for an office.
 * 
 *
 */
public class OfficeQueueManager {
    
    private Map<String,Integer> types = new HashMap<>();
    private Map<String,Set<String>> counters = new HashMap<>();
    private Map<String,List<Integer>> tickets = new HashMap<>();
    private int nextTicket = 1;
    private Map<String,Long> servedByType = new HashMap<>();
    private Map<String,Map<String,Long>> servedByCounter = new HashMap<>();
    private List<QueueListener> listeners = new LinkedList<>();

    /**
     * Define a new type of request.
     * A type is characterized by a label and an
     * estimated time to serve this type of request.
     * 
     * @param label : the label for the request type
     * @param estimatedTime : the estimated time
     */
    public void addRequestType(String label, int estimatedTime) {
        types.put(label, estimatedTime);
    }
    
    /**
     * Returns a list of all defined request types
     * @return
     */
    public Set<String> getRequestTypes(){
        return types.keySet();
    }
    
    /**
     * Adds a new counter that will be managed by this class.
     * The counter is described by a unique id and
     * a set of request type that can be served.
     * 
     * @param id : a unique id for the counter
     * @param canServeTypes : set of request types
     */
    public void addCounter(String id, String... canServeTypes) {
        counters.put(id,Stream.of(canServeTypes).collect(Collectors.toSet()));
    }

    /**
     * Lists all the counters defined.
     * 
     * @return : list of counter ids
     */
    public Collection<String> getCounters(){
        return counters.keySet();
    }
    
    /**
     * Lists all the counters that can serve the 
     * given request type
     * 
     * @param requestType : the type of request to be served
     * @return : list of counter ids
     */
    public Collection<String> getCounters(String requestType){
        return counters.entrySet().stream()
                .filter( e -> e.getValue().contains(requestType))
                .map(Map.Entry::getKey)
                .collect(toList())
                ;
    }

    /**
     * Creates a new ticket for a specific request type.
     * <p>
     * The method returns a new ticket number that must be
     * unique. The first ticket issued must have number 1
     * and the ensuing must be incremented by 1.
     * <p>
     * The method should trigger calling the method
     * {@link QueueListener#updateList} of all listener
     * to notify the change in the queue length.
     * 
     * @param requestType : type of request, must be one of the predefined request types
     * @return : the numeric id of the ticket
     * 
     * @throws QueueException : if the request type is not defined
     */
    public int openTicket(String requestType)  /* throws QueueException */ {
//        if(!this.types.containsKey(requestType)) {
//            throw new QueueException("Request type unknown: " + requestType);
//        }
        tickets.compute(requestType, (k,v) -> {
           if(v==null) v = new LinkedList<>();
           v.add(nextTicket);
           return v;
        });
        listeners.forEach(l -> l.queueUpdate(requestType,tickets.get(requestType).size()));
        return nextTicket++;
    }
    
    /**
     * Returns the length of the pending tickets queue for each
     * type of request.
     * <p>
     * In case of a request type with no pending ticket
     * the length of the queue must be reported as 0.
     * <p>
     * A ticket is considered pending if it has been opened
     * (with method {@link #openTicket}) but not yet
     * called to any counter.
     * 
     * @return the map of length vs. request type
     */
    public Map<String,Long> queueLengths(){
        
        Map<String,Long> res = new HashMap<>();
        tickets.forEach((k,v) -> res.put(k, (long) v.size()));
        return res;
        
//        return tickets.entrySet().stream()
//                .collect(groupingBy(Map.Entry::getKey,
//                                    summingLong(e -> e.getValue().size())))
//                ;
    }
    
    
    /**
     * Signal that the counter is ready to serve next ticket.
     * <p>
     * Based on the pending tickets selects the ticket with 
     * the type corresponding to the longest queue.
     * 
     * If there is no ticket of any of the types served by the counter
     * the method returns 0.
     * <p>
     * The method also marks the previous ticket (if any) as served.
     * <p>
     * This method should trigger the calls, for all listeners,
     * of the methods 
     * <ul>
     *      <li>{@link QueueListener#callTicket} to notify the new ticket to be served
     *      <li>{@link QueueListener#queueUpdate} to update the queue length for the type of ticket to be served
     * </ul>
     *  
     * @param counterId : the counter about to serve the next ticket
     * @return the ticket number to be served or {@code 0} if no ticket available
     */
    public int serveNext(String counterId) {
        Set<String> accept = counters.get(counterId);

        int[] res= {0};
        tickets.entrySet().stream()
        .filter( e -> accept.contains(e.getKey()))
        .max(comparingInt((Map.Entry<String,List<Integer>> e) -> e.getValue().size())
                .thenComparingInt(e -> -types.get(e.getKey())))
        .ifPresent(e -> {
            List<Integer> lt = e.getValue();
            String requestType = e.getKey();
            if(lt.size()>0) {
                res[0] = lt.remove(0);
                servedByType.compute(requestType, (t,els)-> {
                  if(els==null) return 1L;
                  return els+1;
                });
                servedByCounter.compute(counterId, (c,m)-> {
                  if(m==null) m=new HashMap<String,Long>();
                  m.compute(requestType, (t,els)-> {
                      if(els==null) return 1L;
                      return els+1;
                  });
                  return m;
                });
                listeners.forEach(l -> { l.callTicket(res[0], counterId);});
                listeners.forEach(l -> { l.queueUpdate(requestType, lt.size());});
            }
        });
        return res[0];

        // OR
        
//      return tickets.entrySet().stream()
//      .filter( e -> accept.contains(e.getKey()))
//      .sorted(comparingInt((Map.Entry<String,List<Integer>> e) -> e.getValue().size())
//              .thenComparingInt(e -> types.get(e.getKey())))
//      .limit(1)
//              // add to the map of tickets served by type
//      .peek( e -> servedByType.compute(e.getKey(), (t,els)-> {
//                  if(els==null) els=new LinkedList<Integer>();
//                  if(e.getValue().size()>0) els.add(e.getValue().get(0));
//                  return els;
//              }))
//              // add to the map of tickets served by counter
//      .peek( e -> servedByCounter.compute(counterId, (c,m)-> {
//          if(m==null) m=new HashMap<String,List<Integer>>();
//          m.compute(e.getKey(), (t,els)-> {
//              if(els==null) els=new LinkedList<Integer>();
//              if(e.getValue().size()>0) els.add(e.getValue().get(0));
//              return els;
//          });
//          return m;
//      }))
//              // notify
//      .peek( e -> listeners.forEach(l -> { if(e.getValue().size()>0) l.callTicket(e.getValue().get(0), counterId);}))
//      .peek( e -> listeners.forEach(l -> { if(e.getValue().size()>0) l.queueUpdate(e.getKey(), e.getValue().size()-1);}))
//                  // remove if any
//      .mapToInt( e -> e.getValue().size()>0?e.getValue().remove(0):0)
//      .findFirst()
//      .orElse(0);

    }
    
    /**
     * Adds a new listener for the announcements
     * issues by this queue manager.
     * 
     * @param listener : the new listener
     */
    public void addQueueListener(QueueListener listener) {
        if(tickets.size()>0) { // bring the listener up to date with the queues
            tickets.forEach((req,tiks)->listener.queueUpdate(req, tiks.size()));
        }
        listeners.add(listener);
    }
    
    /**
     * Estimates the waiting time remaining for a given ticket number.
     * 
     * @param ticket : the ticket number
     * @return the estimated time
     */
    public double estimatedWaitingTime(int ticket){
//        The estimated waiting time is computer by multiplying the count of tickets preceding
//        the considered one in the list for its request type by the request type estimated
//        serving time an dividing the result by the number of counters capable of serving
//        the same request type. Such a time is further incremented by half the estimated serving time.

        return
        tickets.entrySet().stream()
        .filter(e -> e.getValue().contains(ticket))
        .findFirst()
        .map(e -> {
            String requestType = e.getKey();
            double servingTime = types.get(requestType);
            double ahead = e.getValue().indexOf(ticket);
            double numCounters = getCounters(requestType).size();
            return (ahead*servingTime)/numCounters+servingTime/2;
        }).orElse(-1.0);
        
    }
    
    
    /**
     * Returns the number of tickets served by type.
     * <p>
     * A ticket is considered as served after it has
     * been called to a counter by method {@link #serveNext(String)}
     * 
     * @return the map of count of served tickets vs. the ticket type 
     */
    public Map<String,Long> servedByType(){
        return Collections.unmodifiableMap( servedByType );
    }

    /**
     * Returns the number of tickets served by counter.
     * The count of each counter is reported as a map
     * reporting the number of tickets by type.
     * <p>
     * A ticket is considered as served after it has
     * been called to a counter by method {@link #serveNext(String)}
     * 
     * @return the map of count of served tickets vs. the ticket type vs. the counter.
     */
    public Map<String,Map<String,Long>> servedByCounter(){
        return Collections.unmodifiableMap( servedByCounter );
    }
}
