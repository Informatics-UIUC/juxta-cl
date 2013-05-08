package org.juxtasoftware;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;

public abstract class JuxtaBase {
    protected JuxtaCL juxtaCL = new JuxtaCL();
    protected ByteArrayOutputStream sysOut = new ByteArrayOutputStream();
    protected PrintStream origSysOut;
    File workDir = new File("jx-unittests-work");
    static {
        JuxtaCL.initLogging(true);
    }
    @Before
    public void setup() {
        this.origSysOut = System.out;  
        System.setOut(new PrintStream(this.sysOut));
        workDir = new File("jx-unittests-work");
        workDir.mkdir();
        workDir.deleteOnExit();
    }
    
    @After
    public void teardown() {
        System.setOut(this.origSysOut );
        this.sysOut.reset();
    }

    protected File resourceToFile(String resourceName) throws IOException {
        InputStream is = getClass().getResourceAsStream("/"+resourceName);
        File local = new File(this.workDir, resourceName);
        FileOutputStream fos = new FileOutputStream(local);
        IOUtils.copy(is, fos);
        IOUtils.closeQuietly(fos);
        local.deleteOnExit();
        return local;
    }
}
