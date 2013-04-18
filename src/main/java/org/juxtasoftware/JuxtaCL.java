package org.juxtasoftware;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;


/**
 * Main entry point for JuxtaCL. Initializes spring context and processes command line arguments
 * 
 * @author loufoster
 *
 */
public class JuxtaCL {
    public static ClassPathXmlApplicationContext context;
    
    public static void main(String[] args) throws Exception {
        // be sure to use the saxon parser
        System.setProperty("javax.xml.transform.TransformerFactory", "net.sf.saxon.TransformerFactoryImpl");

        // initialize application context
        initApplicationContext();
        
        LoggerFactory.getLogger(JuxtaCL.class).info("Juxta Web service started");
    }

    private static void initApplicationContext() {
        PropertyConfigurator.configure("config/log4j.properties");
        JuxtaCL.context = new ClassPathXmlApplicationContext(new String[]{
            "applicationContext.xml"});
        JuxtaCL.context.registerShutdownHook();
    }
}
