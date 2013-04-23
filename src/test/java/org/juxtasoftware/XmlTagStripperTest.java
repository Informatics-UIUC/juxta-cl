package org.juxtasoftware;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Unit test for XML tag stripper
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:/applicationContext.xml"})
public class XmlTagStripperTest {
    @Autowired JuxtaCL juxtaCL;

    @Test 
    public void testInvalidPath() {
        boolean caughtException = false;
        try {
            this.juxtaCL.parseArgs(new String[] {"-strip", "/tmp/invalid/file.xml"});
            this.juxtaCL.execute();
        } catch (Exception e ) {
            caughtException = true;
        }
        assertTrue("Accepted invalid file paths", caughtException);
    }
}
