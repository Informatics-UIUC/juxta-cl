package org.juxtasoftware;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;


/**
 * Main entry point for JuxtaCL. Initializes spring context and processes command line arguments.
 * Accepts plain text or XML files. Preliminary XML formats; TEI and Gale
 * 
 * @author loufoster
 *
 */
public class JuxtaCL {
    private ClassPathXmlApplicationContext context;
    private static Logger LOG = LoggerFactory.getLogger(JuxtaCL.class);
    
    public static int main(String[] args) {
        try {
            // init parser and logging
            System.setProperty("javax.xml.transform.TransformerFactory", "net.sf.saxon.TransformerFactoryImpl");
            PropertyConfigurator.configure("config/log4j.properties");
            
            // create an instance of the juxtaCL
            JuxtaCL juxtaCl = new JuxtaCL();
            
            return 0;
            
        } catch (Exception e) {
            LoggerFactory.getLogger(JuxtaCL.class).info("JuxtaCL Failed", e);
            return -1;
        }
    }
    
    /**
     * Create an instance of JuxtaCL and initialize the spring application context
     */
    public JuxtaCL() {
        this.context = new ClassPathXmlApplicationContext(new String[]{
            "applicationContext.xml"});
        this.context.registerShutdownHook();
        LOG.info("JuxtaCL Started");
        
    }
    
    /**
     * Compare 2 files and return the change index
     * 
     * @param filePath1 Absolute path to test file 1
     * @param filePath2 Absolute path to test file 2
     * 
     * @return Change index
     */
    public int compare( String filePath1, String filePath2) {
        LOG.info("Compare "+filePath1+" vs "+filePath2);
        return 0;
    }
}
