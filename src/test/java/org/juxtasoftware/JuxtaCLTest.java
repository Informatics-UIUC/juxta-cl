package org.juxtasoftware;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Unit test for JuxtaCLTest
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:/applicationContext.xml"})
public class JuxtaCLTest {

    @Test void testMissingArgs() {
        JuxtaCL.main( new String[] {});
    }
	 
    @Test
    public void testCompareSame() throws IOException {
        
        File testFile = resourceToFile("roses.txt");
        Configuration config = new Configuration();
        config.addFile(testFile.getPath() );
        config.addFile(testFile.getPath() );
        
        JuxtaCL juxtaCl = new JuxtaCL( config );
        int changeIdx = juxtaCl.compare(testFile.getPath(), testFile.getPath());
        assertTrue(changeIdx == 0);
    }
    
    private File resourceToFile(String resourceName) throws IOException {
        InputStream is = getClass().getResourceAsStream("/"+resourceName);
        File local = File.createTempFile("resource", "dat");
        FileOutputStream fos = new FileOutputStream(local);
        IOUtils.copy(is, fos);
        IOUtils.closeQuietly(fos);
        local.deleteOnExit();
        return local;
    }
}
