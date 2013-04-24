package org.juxtasoftware;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.transform.TransformerException;

import org.apache.commons.cli2.Argument;
import org.apache.commons.cli2.CommandLine;
import org.apache.commons.cli2.Group;
import org.apache.commons.cli2.Option;
import org.apache.commons.cli2.OptionException;
import org.apache.commons.cli2.builder.ArgumentBuilder;
import org.apache.commons.cli2.builder.DefaultOptionBuilder;
import org.apache.commons.cli2.builder.GroupBuilder;
import org.apache.commons.cli2.builder.SwitchBuilder;
import org.apache.commons.cli2.commandline.Parser;
import org.apache.commons.cli2.option.Switch;
import org.apache.commons.cli2.validation.EnumValidator;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.juxtasoftware.Configuration.Hyphens;
import org.juxtasoftware.Configuration.Mode;
import org.xml.sax.SAXException;


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
    private static Logger LOG = Logger.getLogger(JuxtaCL.class);
    private Configuration config;
 
    private String version = "0.1-SNAPSHOT";
    private XmlTagStripper tagStripper;
    
    public static void main(String[] args) {
        try {
            // init parser and logging
            System.setProperty("javax.xml.transform.TransformerFactory", "net.sf.saxon.TransformerFactoryImpl");
            PropertyConfigurator.configure("config/log4j.properties");
            
            // get the application bean
            JuxtaCL juxtaCl = new JuxtaCL();

            // Parse the command line to configure this instance of JuxtaCL
            juxtaCl.parseArgs(args);
            
            // run juxtaCL
            juxtaCl.execute();
            
        }  catch(FileNotFoundException fnf) {
            System.out.println("JuxtaCL Failed - file not found: '"+fnf.getMessage()+"'");
            System.exit(-1);
        } catch (Exception e) {
            LOG.error("JuxtaCL Failed", e);
            System.out.println("JuxtaCL Failed! "+e.getMessage());
            System.exit(-1);
        }
    }
    
    /**
     * JuxtaCL constructor
     */
    public JuxtaCL() {
        this.tagStripper = new XmlTagStripper();
        LOG.info("JuxtaCL started");
    }
    
    /**
     * Parse the commandline options into a configuratin object for this JuxtaCL instance
     * @param args
     * @throws OptionException
     */
    @SuppressWarnings("unchecked")
    public void parseArgs(String[] args) throws OptionException {
        if ( args.length == 0 ) {
            this.config = new Configuration();
            this.config.setMode(Mode.HELP);
            return;
        }
        
        final ArgumentBuilder aBuilder = new ArgumentBuilder();
        final DefaultOptionBuilder oBuilder = new DefaultOptionBuilder();
        final GroupBuilder gBuilder = new GroupBuilder();
        final SwitchBuilder sBuilder = new SwitchBuilder();
        this.config = new Configuration();
        
        Option verbose = oBuilder
                .withShortName("verbose")
                .create();
       
        Option version = oBuilder
            .withShortName("version")
            .create();
        Option help = oBuilder
            .withShortName("help")
            .create();
        
        // XML strip options; 1 file to strip and optional verbose flag
        Group stripOpt = gBuilder.withOption(verbose).create();
        Argument stripArg = aBuilder.withMinimum(1).withMaximum(1).withName("file").create();
        Option strip = oBuilder
            .withShortName("strip")
            .withArgument(stripArg )
            .withChildren(stripOpt)
            .create();
        
        // Diff options; 2 files and optional switches
        Argument diffArg = aBuilder.withMinimum(2).withMaximum(2).withName("file").create();
        Switch punct = sBuilder
            .withName("punct")
            .withRequired(false)
            .withSwitchDefault(true)
            .create();
        Switch caps = sBuilder
            .withName("caps")
            .withRequired(false)
            .withSwitchDefault(true)
            .create();
        
        Set<String> enumSet = new TreeSet<String>();
        enumSet.add("all");
        enumSet.add("linebreak");
        enumSet.add("none");     
        EnumValidator hyphenVal = new EnumValidator( enumSet );
        Option hyphen = oBuilder
          .withShortName("hyphen")
          .withArgument(
              aBuilder
                  .withMinimum(1)
                  .withMaximum(1)
                  .withDefault("all")
                  .withValidator(hyphenVal)
                  .create())
          .create();
        Group diffOpt = gBuilder.withOption(punct).withOption(caps).withOption(verbose).withOption(hyphen).create();
        Option diff = oBuilder
            .withShortName("diff")
            .withArgument(diffArg )
            .withChildren(diffOpt)
            .create();



        Group opts = gBuilder.withOption(strip).withOption(diff).withOption(help).withOption(version).create();

        // parse the options passed in
        Parser parser = new Parser();
        parser.setGroup(opts);
        CommandLine cl = parser.parse(args);
        
        // if version was requested, set mode to version and ignore everything else
        if ( cl.hasOption(version)) {
            
            this.config.setMode(Mode.VERSION);
        } else if ( cl.hasOption(help)) {
         
            this.config.setMode(Mode.HELP);
        } else if ( cl.hasOption(strip)) {
            
            this.config.setVerbose( cl.hasOption(verbose));
            this.config.setMode(Mode.STRIP);
            this.config.addFile( (String)cl.getValue(strip));
        } else{
            
            // extract to diff
            List<String> files = cl.getValues(diff);
            for ( String f : files) {
                this.config.addFile(f);
            }
            
            if ( cl.hasOption(punct)) {
                Boolean p = cl.getSwitch(punct);
                this.config.setIgnorePunctuation(!p);
            }
            if ( cl.hasOption(caps)) {
                Boolean p = cl.getSwitch(caps);
                this.config.setIgnoreCase(!p);
            }

            if ( cl.hasOption(hyphen)) {
                String hs = (String)cl.getValue(hyphen);
                Hyphens hFilter = Hyphens.valueOf(hs.toUpperCase());
                this.config.setHyphenation(hFilter);
            }
             
            if ( cl.hasOption(verbose)) {
                this.config.setVerbose(true);
            }
        }
        
        if ( this.config.isVerbose() ) {
            displayConfiguration(this.config);
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
     * Back door for unit tests to test cfg
     * @param cfg
     */
    protected Configuration getConfig() {
        return this.config;
    }

    /**
     * helper to dump configuration information to console
     * @param cfg
     */
    private void displayConfiguration(Configuration cfg) {
        if ( cfg.getMode().equals(Mode.CHANGE_INDEX)) {
            System.out.println("Collation Configuration: ");
            System.out.println("   Comparand          : " + cfg.getFiles().get(0));
            System.out.println("   Comparand          : " + cfg.getFiles().get(1));
            System.out.println("   Ignore Case        : " + cfg.isIgnoreCase() );
            System.out.println("   Ignore Punctuation : " + cfg.isIgnorePunctuation());
            System.out.println("   Hyphenation        : " + cfg.getHyphenation());
        } else {
            System.out.println("Tag Strip Configuration: ");
            System.out.println("   File : " + cfg.getFiles().get(0));
        }
    }
    
    /**
     * Perform transformation / tokenizaton / collation based upon parsed config.
     * Results posted to std:out
     * @throws IOException 
     */
    public void execute() throws IOException {
        if ( this.config.getMode().equals(Mode.VERSION)) {
            System.out.println("JuxtaCL Version "+this.version);
        } else if (this.config.getMode().equals(Mode.HELP)) {
            
            displayHelp();
        } else if ( this.config.getMode().equals(Mode.STRIP)){
            // strip XML tags and dump plain text to std::out
            String txtContent = doTagStrip();
            System.out.println(txtContent);
        } else {
            // compare the two files and dump change index to std:out
            doComparison();
        }
    }
    
    private void displayHelp() {
        StringBuilder out = new StringBuilder("Usage: juxta [-diff file1 file2 [options]] | [-strip file]\n");
        out.append("  Options:\n");
        out.append("    (+|-)punct                     : Toggle punctuation filtering\n");
        out.append("                                       defaults to ignore punctuation\n");  
        out.append("    (+|-)caps                      : Toggle case sensitivity\n");
        out.append("                                       defaults to case insensitive\n");  
        out.append("    -hyphens (all|linebreak|none)  : Hyphenation inclusion setting\n");
        out.append("                                       defaults to include all\n");  
        out.append("    -verbose                       : Show collation details\n");
        System.out.println(out.toString());
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
    
    /**
     * Strip all XML tags and return plain text string
     * 
     * @return
     * @throws IOException 
     */
    protected String doTagStrip() throws IOException {
        LOG.info("Strip XML tags from "+this.config.getFiles().get(0));
        File a = new File(this.config.getFiles().get(0));
        if ( a.exists() == false ) {
            LOG.error("Tag Strip Failed. '"+a+"' does not exist");
            throw new FileNotFoundException(a.getPath());
        }
        
        LOG.info("Normalizing input file encoding");
        File workFile = createCleanWorkFile(a);
        try {
            return this.tagStripper.stripTags(workFile);
        } catch (SAXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (TransformerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return "";
    }
    
    /**
     * Clone the source file into a temporary working file that the JuxtaCL will
     * manipulate during processing.
     * 
     * @param src
     * @return
     * @throws IOException
     */
    private File createCleanWorkFile( File src ) throws IOException {
        FileInputStream fis = new FileInputStream(src);
        File workFile = EncodingUtils.fixEncoding(fis);
        IOUtils.closeQuietly(fis);
        workFile.deleteOnExit();
        return workFile;
    }
}
