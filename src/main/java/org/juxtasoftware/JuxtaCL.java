package org.juxtasoftware;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import org.apache.commons.cli2.Argument;
import org.apache.commons.cli2.CommandLine;
import org.apache.commons.cli2.Group;
import org.apache.commons.cli2.OptionException;
import org.apache.commons.cli2.builder.ArgumentBuilder;
import org.apache.commons.cli2.builder.GroupBuilder;
import org.apache.commons.cli2.commandline.Parser;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;


/**
 * Main entry point for JuxtaCL. Initializes spring context and processes command line arguments.
 * Accepts plain text or XML files. Preliminary XML formats; TEI and Gale
 * 
 * Optional parameters specify tokenization / collation settings. These include:
 *   Capitalization
 *   Punctuation
 *   Hyphen sensitivity
 * 
 * By defaut, caps and punctuation are ignored and all hypens are included
 * 
 * @author loufoster
 *
 */
public class JuxtaCL {
    private ClassPathXmlApplicationContext context;
    private static Logger LOG = LoggerFactory.getLogger(JuxtaCL.class);
    private final Configuration config;
    
    public static void main(String[] args) {
        try {
            // init parser and logging
            System.setProperty("javax.xml.transform.TransformerFactory", "net.sf.saxon.TransformerFactoryImpl");
            PropertyConfigurator.configure("config/log4j.properties");
            
            // Parse the command line into a configuration object
            Configuration config = parseArgs(args);
            
            // create an instance of the juxtaCL
            JuxtaCL juxtaCl = new JuxtaCL( config );
            int ci = juxtaCl.compare();
            System.out.println("Change Index="+ci);
            
        } catch (FileNotFoundException fnf) {
            System.out.println("File not found: '"+fnf.getMessage()+"'");
            System.exit(-1);
        } catch (Exception e) {
            LoggerFactory.getLogger(JuxtaCL.class).info("JuxtaCL Failed", e);
            System.out.println("Juxta CL Failed! "+e.toString());
            System.exit(-1);
        }
    }
    
    protected static Configuration parseArgs(String[] args) throws OptionException {
        final ArgumentBuilder aBuilder = new ArgumentBuilder();
//        final DefaultOptionBuilder oBuilder = new DefaultOptionBuilder();
        final GroupBuilder gBuilder = new GroupBuilder();
//        
        Argument fileArg = aBuilder.withMinimum(2).withMaximum(2).withName("comparand").create();
//        Option files = oBuilder
//            .withArgument(fileArg)
//            .withShortName("f")
//            .withLongName("files")
//            .withDescription("The files to be compared")
//            .withRequired(true)
//            .create();
        Group opts = gBuilder.withOption(fileArg).create();
//
        Parser parser = new Parser();
        parser.setGroup(opts);
        CommandLine cl = parser.parse(args);
        Configuration config = new Configuration();
        
        // extract files
        @SuppressWarnings("unchecked")
        List<String> comparands = cl.getValues("comparand");
        for ( String c : comparands) {
            config.addFile(c);
        }
        
        return config;
    }

    /**
     * Create an instance of JuxtaCL and initialize the spring application context
     * @param config configuration for the comparison
     */
    public JuxtaCL(Configuration config) {
        this.config = config;
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
     * @throws FileNotFoundException 
     */
    public int compare() throws FileNotFoundException {
        LOG.info("Compare "+this.config.getFiles());
        File a = new File(this.config.getFiles().get(0));
        File b = new File(this.config.getFiles().get(1));
        if ( a.exists() == false ) {
            LOG.error("Compare Failed. '"+a+"' does not exist");
            throw new FileNotFoundException(a.getPath());
        }
        if ( b.exists() == false ) {
            LOG.error("Compare Failed. '"+b+"' does not exist");
            throw new FileNotFoundException(b.getPath());
        }
        
        return 0;
    }
}
