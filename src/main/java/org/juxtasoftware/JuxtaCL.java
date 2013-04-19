package org.juxtasoftware;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import org.apache.commons.cli2.Argument;
import org.apache.commons.cli2.CommandLine;
import org.apache.commons.cli2.Group;
import org.apache.commons.cli2.Option;
import org.apache.commons.cli2.OptionException;
import org.apache.commons.cli2.builder.ArgumentBuilder;
import org.apache.commons.cli2.builder.DefaultOptionBuilder;
import org.apache.commons.cli2.builder.GroupBuilder;
import org.apache.commons.cli2.commandline.Parser;
import org.apache.log4j.PropertyConfigurator;
import org.juxtasoftware.Configuration.HyphenationFilter;
import org.juxtasoftware.Configuration.Mode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;


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
@Component
public class JuxtaCL {
    private static Logger LOG = LoggerFactory.getLogger(JuxtaCL.class);
    private Configuration config;
    
    @Autowired
    @Qualifier("version")
    private String version;
    
    public static void main(String[] args) {
        try {
            // init parser and logging
            System.setProperty("javax.xml.transform.TransformerFactory", "net.sf.saxon.TransformerFactoryImpl");
            PropertyConfigurator.configure("config/log4j.properties");
            ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("/applicationContext.xml");
            context.registerShutdownHook();
            
            // get the application bean
            JuxtaCL juxtaCl = (JuxtaCL)context.getBean(JuxtaCL.class);

            // Parse the command line to configure this instance of JuxtaCL
            juxtaCl.parseArgs(args);
            
            // run juxtaCL
            juxtaCl.execute();
            
        }  catch(FileNotFoundException fnf) {
            System.out.println("Juxta CL Failed - file not found: '"+fnf.getMessage()+"'");
            System.exit(-1);
        } catch (Exception e) {
            LoggerFactory.getLogger(JuxtaCL.class).info("JuxtaCL Failed", e);
            System.out.println("Juxta CL Failed! "+e.getMessage());
            System.exit(-1);
        }
    }
    
    /**
     * Parse the commandline options into a configuratin object for this JuxtaCL instance
     * @param args
     * @throws OptionException
     */
    @SuppressWarnings("unchecked")
    public void parseArgs(String[] args) throws OptionException {
        final ArgumentBuilder aBuilder = new ArgumentBuilder();
        final DefaultOptionBuilder oBuilder = new DefaultOptionBuilder();
        final GroupBuilder gBuilder = new GroupBuilder();
        this.config = new Configuration();

        Argument fileArg = aBuilder.withMinimum(0).withMaximum(2).withName("comparand").create();
        
        Option verbose = oBuilder
                .withLongName("verbose")
                .withShortName("v")
                .withDescription("display collation details")
                .create();
        Option version = oBuilder
            .withLongName("version")
            .withDescription("display JuxtaCL version information")
            .create();
        Option punct = oBuilder
            .withLongName("ignore-punctuation")
            .withShortName("p")
            .withDescription("Toggle punctuation filtering")
            .withArgument(
                aBuilder
                    .withMinimum(1)
                    .withMaximum(1)
                    .create())
            .create();
        Option caps = oBuilder
            .withLongName("ignore-case")
            .withShortName("c")
            .withDescription("Toggle case sensitivity")
            .withArgument(
                aBuilder
                    .withMinimum(1)
                    .withMaximum(1)
                    .create())
            .create();
        Option hyphen = oBuilder
            .withLongName("hyphens")
            .withShortName("c")
            .withDescription("Hyphenation settings")
            .withArgument(
                aBuilder
                    .withMinimum(1)
                    .withMaximum(1)
                    .create())
            .create();
        Group opts = gBuilder
            .withOption(fileArg)
            .withOption(punct)
            .withOption(caps)
            .withOption(hyphen)
            .withOption(verbose)
            .withOption(version)
            .create();

        // parse the options passed in
        Parser parser = new Parser();
        parser.setGroup(opts);
        CommandLine cl = parser.parse(args);
        
        // if version was requested, set mode to version and ignore everything else
        if ( cl.hasOption(version)) {
            this.config.setMode(Mode.VERSION);
        } else {
            
            // extract files
            List<String> comparands = cl.getValues("comparand");
            for ( String c : comparands) {
                this.config.addFile(c);
            }
            if ( this.config.getFiles().size() < 2) {
                throw new RuntimeException("Must have two comparands");
            }
            
            if ( cl.hasOption(punct)) {
                Boolean ignorePunct = Boolean.valueOf((String)cl.getValue(punct));
                this.config.setIgnorePunctuation(ignorePunct);
            }
            
            if ( cl.hasOption(caps)) {
                Boolean ignoreCaps = Boolean.valueOf((String)cl.getValue(caps));
                this.config.setIgnoreCase(ignoreCaps);
            }
            
            if ( cl.hasOption(hyphen)) {
                String hs = (String)cl.getValue(hyphen);
                HyphenationFilter hFilter = HyphenationFilter.valueOf(hs.toUpperCase());
                if ( hFilter == null ) {
                    throw new RuntimeException("Invalid hypnenation setting");
                }
            }
             
            if ( cl.hasOption(verbose)) {
                this.config.setVerbose(true);
            }
            
            if ( this.config.isVerbose() ) {
                displayConfiguration(config);
            }
        }
    }
    
    /**
     * Back door for unit tests to inject cfg without parse
     * @param cfg
     */
    protected void setConfig(Configuration cfg) {
        this.config = cfg;
    }

    /**
     * helper to dump configuration information to console
     * @param cfg
     */
    private void displayConfiguration(Configuration cfg) {
        System.out.println("Configuration: ");
        System.out.println("   Comparand          : " + cfg.getFiles().get(0));
        System.out.println("   Comparand          : " + cfg.getFiles().get(1));
        System.out.println("   Ignore Case        : " + cfg.isIgnoreCase() );
        System.out.println("   Ignore Punctuation : " + cfg.isIgnorePunctuation());
        System.out.println("   Hyphenation        : " + cfg.getHyphenationFilter());
        System.out.println("   Mode               : " + cfg.getMode());
    }
    
    /**
     * Perform transformation / tokenizaton / collation based upon parsed config.
     * Results posted to std:out
     * @throws FileNotFoundException 
     */
    public void execute() throws FileNotFoundException {
        if ( this.config.getMode().equals(Mode.VERSION)) {
            System.out.println("JuxtaCL Version "+this.version);
        } else {
            // compare the two files and dump change index to std:out
            doComparison();
        }
    }
    
    /**
     * Compare 2 files and return the change index based on the configuration
     * settings collected from the command line
     * 
     * @param filePath1 Absolute path to test file 1
     * @param filePath2 Absolute path to test file 2
     * 
     * @return Change index
     * @throws FileNotFoundException 
     */
    protected int doComparison() throws FileNotFoundException {
        LOG.info("Compare "+this.config.getFiles());
        File a = new File(this.config.getFiles().get(0));
        File b = new File(this.config.getFiles().get(1));
        
        // be sure both files exist
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
