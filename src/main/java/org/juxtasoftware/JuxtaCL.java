package org.juxtasoftware;

import org.apache.log4j.PropertyConfigurator;
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
    public static ClassPathXmlApplicationContext context;
    
    public static int main(String[] args) {
        try {
            // be sure to use the saxon parser
            System.setProperty("javax.xml.transform.TransformerFactory", "net.sf.saxon.TransformerFactoryImpl");
    
            // initialize application context
            initApplicationContext();
        } catch (Exception e) {
            LoggerFactory.getLogger(JuxtaCL.class).info("JuxtaCL Failed", e);
            return -1;
        }
        
        LoggerFactory.getLogger(JuxtaCL.class).info("JuxtaCL Started");
        return 0;
    }

    private static void initApplicationContext() {
        PropertyConfigurator.configure("config/log4j.properties");
        JuxtaCL.context = new ClassPathXmlApplicationContext(new String[]{
            "applicationContext.xml"});
        JuxtaCL.context.registerShutdownHook();
    }
}
