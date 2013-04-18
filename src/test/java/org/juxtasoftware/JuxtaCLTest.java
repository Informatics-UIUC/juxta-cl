package org.juxtasoftware;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;

/**
 * Unit test for JuxtaCLTest
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:/applicationContext.xml"})
public class JuxtaCLTest {

    @Test
    public void testApp() {
        int out = JuxtaCL.main(null);
        assertTrue(out == 0);
    }
}
