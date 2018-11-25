package it.polito.po.test;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;

import it.polito.oop.office.OfficeQueueManager;
import it.polito.oop.office.QueueException;
import it.polito.oop.office.QueueListener;

public class TestR4_Notifications {


    private static final String OTHER = "Other";
    private static final String SAVINGS = "Savings";
    private static final String PACKAGES = "Packages";


    private OfficeQueueManager manager;
    
    private Listener l;
    
    /**
     * This is a fake notification listener that just
     * records the notifications so the test can check
     * they were sent out as expected.
     *
     */
    private static class Listener implements QueueListener {

        Map<Integer,String> assignments = new LinkedHashMap<>();
        @Override
        public void callTicket(int ticketNo, String counterId) {
            //System.err.println("Call ticket :" + ticketNo + " ==> " + counterId);
            assignments.put(ticketNo, counterId);
        }

        Map<String,Long> queues = new HashMap<>();
        @Override
        public void queueUpdate(String ticketType, long queueLenght) {
            //System.err.println("Updated queue :" + ticketType + " = " + queueLenght);
            queues.put(ticketType, queueLenght);
        }
        
    }
    
    @Before
    public void setUp() throws QueueException {        
        manager = new OfficeQueueManager();
        manager.addRequestType(PACKAGES, 10);
        manager.addRequestType(SAVINGS, 8);
        manager.addRequestType(OTHER, 6);

        manager.addCounter("P1", PACKAGES); 
        manager.addCounter("P2", PACKAGES,SAVINGS); 
        manager.addCounter("S1", SAVINGS); 
        manager.addCounter("VV", PACKAGES,SAVINGS,OTHER); 
        
        l = new Listener();
        manager.addQueueListener(l);
        
    }

    @Test
    public void testNotifyUpdate() throws QueueException {
        manager.openTicket(PACKAGES);
        
        assertTrue("Missing notification of changed queue length",l.queues.containsKey(PACKAGES));
        
        assertEquals("Missing notification of changed queue length",
                                1L,
                                l.queues.get(PACKAGES).longValue());        
    }

    @Test
    public void testNotifyUpdate2() throws QueueException {
        manager.openTicket(PACKAGES);
        manager.openTicket(SAVINGS);
        manager.openTicket(PACKAGES);
        manager.openTicket(SAVINGS);
        manager.openTicket(OTHER);
        manager.openTicket(PACKAGES);
        
        assertTrue("Missing notification of changed queue length",l.queues.containsKey(PACKAGES));
        assertTrue("Missing notification of changed queue length",l.queues.containsKey(SAVINGS));
        assertTrue("Missing notification of changed queue length",l.queues.containsKey(OTHER));
        
        assertEquals("Missing notification of changed queue length",
                                3L,
                                l.queues.get(PACKAGES).longValue());        
        assertEquals("Missing notification of changed queue length",
                2,
                l.queues.get(SAVINGS).longValue());        
        assertEquals("Missing notification of changed queue length",
                1,
                l.queues.get(OTHER).longValue());        
    }

    @Test
    public void testNotifyTicket() throws QueueException {
        manager.openTicket(PACKAGES);

        manager.serveNext("P1");
        
        assertTrue("Missing notification of changed queue length",
                    l.assignments.containsKey(1));
        
        assertEquals("Missing notification of changed queue length",
                                "P1",
                                l.assignments.get(1));        

    }

    @Test
    public void testNotifyTicket2() throws QueueException {
        manager.openTicket(PACKAGES);
        manager.openTicket(SAVINGS);
        manager.openTicket(PACKAGES);
        manager.openTicket(SAVINGS);
        manager.openTicket(PACKAGES);

        manager.serveNext("P1"); // 1
        manager.serveNext("S1"); // 2
        manager.serveNext("P2"); // 3 
        manager.serveNext("VV"); // 4
        
        
        int noTicket = Stream.of(1,2,3,4)
        .filter(t-> ! l.assignments.containsKey(t))
        .findAny().orElse(-1);
        assertEquals("Missing ticket in notification: ",-1,noTicket);

        String err = Stream.of("P1:1","S1:2","P2:3","VV:4")
        .filter(s->!l.assignments.get(Integer.parseInt(s.split(":")[1])).equals(s.split(":")[0]))
        .findAny().orElse(null);
        assertNull("Wrong notification for ticket: " + err,err);
        
    }

}
