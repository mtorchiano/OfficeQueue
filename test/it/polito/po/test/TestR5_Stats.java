package it.polito.po.test;

import static org.junit.Assert.*;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import it.polito.oop.office.OfficeQueueManager;
import it.polito.oop.office.QueueException;

public class TestR5_Stats {
    private static final String OTHER = "Other";
    private static final String SAVINGS = "Savings";
    private static final String PACKAGES = "Packages";


    private OfficeQueueManager manager;
    
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
        manager.openTicket(PACKAGES);  // 1
        manager.openTicket(SAVINGS);   // 2
        manager.openTicket(PACKAGES);  // 3
        manager.openTicket(SAVINGS);   // 4
        manager.openTicket(OTHER);     // 5
        manager.openTicket(PACKAGES);  // 6
        
    }
    
    @Test
    public void testWaitingTime() throws QueueException {
        
        double wt = manager.estimatedWaitingTime(1);
        
        assertEquals("Wrong estimated waiting time",5.0,wt,0.01);
        
    }

    @Test
    public void testWaitingTime1() throws QueueException {
        
        manager.serveNext("P2");  // 2
        double wt = manager.estimatedWaitingTime(4);
        
        assertEquals("Wrong estimated waiting time",6.66,wt,0.01);
        
    }

    @Test
    public void testServedByType() throws QueueException {
        
        manager.serveNext("P1");  // 1 PACKAGES
        manager.serveNext("P2");  // 2 SAVINGS
        manager.serveNext("S1");  // 4 SAVINGS
        manager.serveNext("P1");  // 3 PACKAGES
        manager.serveNext("VV");  // 5 OTHER
        
        Map<String,Long> served = manager.servedByType();
        
        assertNotNull(served);
        
        assertTrue("Missing request type " + PACKAGES,served.containsKey(PACKAGES));
        assertEquals("Wrong count for type " + PACKAGES,2,served.get(PACKAGES).longValue());
        
        assertTrue("Missing request type " + SAVINGS,served.containsKey(SAVINGS));
        assertEquals("Wrong count for type " + SAVINGS,2,served.get(SAVINGS).longValue());
        
    }

    
    @Test
    public void testServedByCounter() throws QueueException {
        
        manager.serveNext("P1");  // 1
        manager.serveNext("P2");  // 2
        manager.serveNext("S1");  // 4
        manager.serveNext("P1");  // 3
        manager.serveNext("VV"); // 5
        
        Map<String,Map<String,Long>> served = manager.servedByCounter();
        
        assertNotNull("No served by counter stat",served);
        
        assertTrue("Missing counter P1",served.containsKey("P1"));
        assertEquals("Wrong count for " + PACKAGES + " at counter P1",
                        2,served.get("P1").get(PACKAGES).longValue());
        
        assertTrue("Missing counter P2",served.containsKey("P2"));
        assertEquals("Wrong count for " + PACKAGES + " served by counter P2",
                        0,served.get("P2").getOrDefault(PACKAGES, new Long(0)).longValue());
        assertEquals("Wrong count for " + SAVINGS + " served by counter P2",
                1,served.get("P2").get(SAVINGS).longValue());
        
    }

}
