package it.polito.po.test;

import static org.junit.Assert.*;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import it.polito.oop.office.OfficeQueueManager;
import it.polito.oop.office.QueueException;

public class TestR3_Classes {

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
    public void testServe() throws QueueException {
        int ticket = manager.serveNext("P1");
        
        assertEquals("Wrong ticket",1,ticket);
    }

    @Test
    public void testServe1() throws QueueException {
        int ticket = manager.serveNext("S1");
        
        assertEquals("Wrong ticket",2,ticket);
    }

    @Test
    public void testServe2() throws QueueException {
        int ticket = manager.serveNext("P1");
        ticket = manager.serveNext("P1");
        
        assertEquals("Wrong ticket",3,ticket);
    }

    @Test
    public void testServe3() throws QueueException {
        int ticket = manager.serveNext("P1");  // 1
        assertEquals("Wrong ticket",1,ticket);

        ticket = manager.serveNext("P2");  // 2
        assertEquals("Wrong ticket",2,ticket);

        ticket = manager.serveNext("S1");  // 4
        assertEquals("Wrong ticket",4,ticket);

        ticket = manager.serveNext("P1");  // 3
        assertEquals("Wrong ticket",3,ticket);

        ticket = manager.serveNext("VV"); // 5        
        assertEquals("Wrong ticket",5,ticket);
    }
 
    @Test
    public void testServe4() throws QueueException {
        int ticket = manager.serveNext("P1");  // 1
        ticket = manager.serveNext("P2");  // 2
        ticket = manager.serveNext("S1");  // 4
        ticket = manager.serveNext("P1");  // 3
        ticket = manager.serveNext("VV"); // 5
        ticket = manager.serveNext("P2");  // 6
        
        ticket = manager.serveNext("P2");  // 0
        
        assertEquals("All queues should be empty",0,ticket);
    }
    
    @Test
    public void testServe5() throws QueueException {
        int ticket = manager.serveNext("S1");  // 2
        ticket = manager.serveNext("S1");  // 4
        ticket = manager.serveNext("S1");  // 4
        
        assertEquals("Queue of reqs served by S1 should be empty",0,ticket);
    }
    
    @Test
    public void testLengths() throws QueueException {
        manager.serveNext("P2");
        manager.serveNext("P2");
        
        Map<String,Long> numbers = manager.queueLengths();
        
        assertNotNull("Missing requests",numbers);
        assertEquals("Missing queue",3,numbers.size());
        assertEquals("Wrong numbers for " + PACKAGES,
                        2L,numbers.get(PACKAGES).longValue());    
        assertEquals("Wrong numbers for " + SAVINGS,
                1L,numbers.get(SAVINGS).longValue());    
        assertEquals("Wrong numbers for " + OTHER,
                1L,numbers.get(OTHER).longValue());    

    }


}
