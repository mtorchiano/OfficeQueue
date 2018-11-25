package it.polito.po.test;

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import it.polito.oop.office.OfficeQueueManager;

public class TestR1_Requests {
    
    private OfficeQueueManager manager;
    
    @Before
    public void setUp() {
        manager = new OfficeQueueManager();
    }

    @Test
    public void testAddRequestType() {
        manager.addRequestType("Packages", 10);
        Set<String> requests = manager.getRequestTypes();
        
        assertNotNull("Missing requests set",requests);
        assertEquals("Missing requests",1,requests.size());
    }

    @Test
    public void testGetRequests() {
        manager.addRequestType("Packages", 10);
        manager.addRequestType("Savings", 8);
        manager.addRequestType("Other", 6);
        Set<String> reqs = manager.getRequestTypes();
        
        assertNotNull("Missing course set",reqs);
        assertEquals("Missing courses",3,reqs.size());
        assertTrue("Missing request type Packages",reqs.contains("Packages"));
        assertTrue("Missing request type Savings",reqs.contains("Savings"));
        assertTrue("Missing request type Other",reqs.contains("Other"));
    }
    
    @Test
    public void testAddCounter() {
        manager.addRequestType("Packages", 10);
        manager.addCounter("P1", "Packages"); 
        
        Collection<String> counters = manager.getCounters();
        
        assertNotNull("Missing counter list",counters);
        assertEquals("Missing counter ",1,counters.size());
    }

    @Test
    public void testGetCounters() {
        manager.addRequestType("Packages", 10);
        manager.addRequestType("Savings", 8);
        manager.addRequestType("Other", 6);

        manager.addCounter("P1", "Packages"); 
        manager.addCounter("S1", "Savings"); 
        manager.addCounter("VV", "Packages","Savings","Other"); 
        
        Collection<String> counters = manager.getCounters();
        
        assertNotNull("Missing counter list",counters);
        assertEquals("Missing counters ",3,counters.size());
        
        assertTrue("Missing counter",counters.contains("P1"));
        assertTrue("Missing counter",counters.contains("S1"));
        assertTrue("Missing counter",counters.contains("VV"));
    }

    @Test
    public void testGetCounterType() {
        manager.addRequestType("Packages", 10);
        manager.addRequestType("Savings", 8);
        manager.addRequestType("Other", 6);

        manager.addCounter("P1", "Packages"); 
        manager.addCounter("P2", "Packages","Savings"); 
        manager.addCounter("S1", "Savings"); 
        manager.addCounter("VV", "Packages","Savings","Other"); 
        
        Collection<String> counters = manager.getCounters("Packages");
        
        assertNotNull("Missing counter list",counters);
        assertEquals("Missing counters ",3,counters.size());
        assertTrue("Missing counter",counters.contains("P1"));
        assertTrue("Missing counter",counters.contains("P2"));
        assertTrue("Missing counter",counters.contains("VV"));    
        
    }

    @Test
    public void testGetCounterType2() {
        manager.addRequestType("Packages", 10);
        manager.addRequestType("Savings", 8);
        manager.addRequestType("Other", 6);

        manager.addCounter("P1", "Packages"); 
        manager.addCounter("P2", "Packages","Savings"); 
        manager.addCounter("S1", "Savings"); 
        manager.addCounter("VV", "Packages","Savings","Other"); 
        
        Collection<String> counters = manager.getCounters("Other");
        
        assertNotNull("Missing counter list",counters);
        assertEquals("Missing counters ",1,counters.size());
        assertTrue("Missing counter",counters.contains("VV"));
        
    }
}