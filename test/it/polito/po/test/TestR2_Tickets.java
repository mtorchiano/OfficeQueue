package it.polito.po.test;

import static org.junit.Assert.*;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import it.polito.oop.office.OfficeQueueManager;
import it.polito.oop.office.QueueException;

public class TestR2_Tickets {

    private static final String OTHER = "Other";
    private static final String SAVINGS = "Savings";
    private static final String PACKAGES = "Packages";
    private OfficeQueueManager manager;
    
    @Before
    public void setUp() {
        manager = new OfficeQueueManager();
        manager.addRequestType(PACKAGES, 10);
        manager.addRequestType(SAVINGS, 8);
        manager.addRequestType(OTHER, 6);

        manager.addCounter("P1", PACKAGES); 
        manager.addCounter("P2", PACKAGES,SAVINGS); 
        manager.addCounter("S1", SAVINGS); 
        manager.addCounter("VV", PACKAGES,SAVINGS,OTHER); 
    }

    @Test
    public void testOpenTicket() throws QueueException {
        int n = manager.openTicket(PACKAGES);
        
        assertEquals("Wrong ticket number",1,n);
    }

    @Test
    public void testOpenTicket2() throws QueueException {
        manager.openTicket(PACKAGES);
        int n = manager.openTicket(SAVINGS);
        
        assertEquals("Wrong ticket number",2,n);
    }

//    @Test
//    public void testOpenTicketErr() {
//        try {
//            manager.openTicket("undefined");
//            fail("Request type must be defined");
//        } catch (QueueException e) {
//            // OK
//        }
//    }

    
    @Test
    public void testQueues() throws QueueException {
        manager.openTicket(PACKAGES);
        manager.openTicket(SAVINGS);
        manager.openTicket(PACKAGES);
        manager.openTicket(SAVINGS);
        manager.openTicket(OTHER);
        manager.openTicket(PACKAGES);
        
        Map<String,Long> numbers = manager.queueLengths();
        
        assertNotNull("Missing requests",numbers);
        assertEquals("Missing queue",3,numbers.size());
        assertEquals("Wrong numbers for " + PACKAGES,
                        3L,numbers.get(PACKAGES).longValue());    
        assertEquals("Wrong numbers for " + SAVINGS,
                2L,numbers.get(SAVINGS).longValue());    
        assertEquals("Wrong numbers for " + OTHER,
                1L,numbers.get(OTHER).longValue());    
    }


}
