package it.polito.po.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ 
    TestR1_Requests.class, 
    TestR2_Tickets.class, 
    TestR3_Classes.class, 
    TestR4_Notifications.class,
    TestR5_Stats.class 
    })
public class AllTests {
}
