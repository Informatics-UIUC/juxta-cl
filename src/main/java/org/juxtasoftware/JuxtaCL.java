package org.juxtasoftware;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

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
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.juxtasoftware.model.Configuration;
import org.juxtasoftware.model.Configuration.Algorithm;
import org.juxtasoftware.model.Configuration.Hyphens;
import org.juxtasoftware.model.Configuration.Mode;
import org.juxtasoftware.model.DiffException;
import org.juxtasoftware.model.EncodingException;
import org.juxtasoftware.model.TagStripException;
import org.juxtasoftware.util.EncodingUtils;
import org.juxtasoftware.util.XmlUtils;


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
 
    private String version = "1.0";
    private XmlTagStripper tagStripper;
    private Tokenizer tokenizer;
    private DiffCollator diffCollator;
    
    public static void main(String[] args) {
        try {
            // init parser and logging
            System.setProperty("javax.xml.transform.TransformerFactory", "net.sf.saxon.TransformerFactoryImpl");
            
            // default to no logging
            LogManager.getRootLogger().removeAllAppenders();
            LogManager.getRootLogger().setLevel(Level.OFF);
            
            // get the application bean
            JuxtaCL juxtaCl = new JuxtaCL();

            // Parse the command line to configure this instance of JuxtaCL
            juxtaCl.parseArgs(args);
            JuxtaCL.initLogging( juxtaCl.config.isVerbose() );
            
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
    
    protected static void initLogging( boolean verbose ) {
        if ( verbose == false ) {
            LogManager.getRootLogger().removeAllAppenders();
            LogManager.getRootLogger().setLevel(Level.OFF);
        } else {
            LogManager.getRootLogger().removeAllAppenders();
            ConsoleAppender console = new ConsoleAppender(new PatternLayout("%m%n")); 
            console.setThreshold(Level.DEBUG);
            console.activateOptions();
            LogManager.getRootLogger().addAppender(console);
            LogManager.getRootLogger().setLevel(Level.DEBUG);
        }
    }
    
    /**
     * JuxtaCL constructor
     */
    public JuxtaCL() {
        this.tagStripper = new XmlTagStripper();
        this.tokenizer = new Tokenizer();
        this.diffCollator = new DiffCollator();
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
        Argument fileArg = aBuilder.withMinimum(1).withMaximum(1).withName("file").create();
        Option strip = oBuilder
            .withShortName("strip")
            .withArgument(fileArg )
            .withChildren(stripOpt)
            .create();
        
        // XML validate optis
        Option validate = oBuilder
            .withShortName("validate")
            .withArgument(fileArg )
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
        
        // hyphenation settings
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
        
        // algorithm
        Set<String> diffSet = new TreeSet<String>();
        diffSet.add("juxta");
        diffSet.add("levenshtein");
        diffSet.add("jaro_winkler");
        EnumValidator algoVal = new EnumValidator( diffSet );
        Option algo = oBuilder
          .withShortName("algorithm")
          .withArgument(
              aBuilder
                  .withMinimum(0)
                  .withMaximum(1)
                  .withDefault("juxta")
                  .withValidator(algoVal)
                  .create())
          .create();
        
        Option normal = oBuilder
            .withShortName("normalize")
            .create();
        
        // put together the diff options
        Group diffOpt = gBuilder.withOption(punct).withOption(caps).withOption(verbose).withOption(hyphen).withOption(algo).withOption(normal).create();
        Option diff = oBuilder
            .withShortName("diff")
            .withArgument(diffArg )
            .withChildren(diffOpt)
            .create();

        // glom all of the options together into the main grouping. it will be used for the parse.
        Group opts = gBuilder
            .withOption(strip)
            .withOption(diff)
            .withOption(validate)
            .withOption(help)
            .withOption(version).create();

        // parse the options passed in
        Parser parser = new Parser();
        parser.setGroup(opts);
        CommandLine cl = parser.parse(args);
        
        // if version was requested, set mode to version and ignore everything else
        if ( cl.hasOption(version)) {
            
            this.config.setMode(Mode.VERSION);
        } else if ( cl.hasOption(help)) {
         
            this.config.setMode(Mode.HELP);
        } else if ( cl.hasOption(validate)) {
         
            this.config.setMode(Mode.VALIDATE);
            this.config.addFile( (String)cl.getValue(validate));
        }else if ( cl.hasOption(strip)) {
            
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
            
            if ( cl.hasOption(algo)) {
                String a = (String)cl.getValue(algo);
                Algorithm ciAlgo = Algorithm.valueOf(a.toUpperCase());
                this.config.setAlgorithm(ciAlgo);
            }
            
            if ( cl.hasOption(normal)) {
                this.config.setNormalizeEncoding(true);
            }
             
            if ( cl.hasOption(verbose)) {
                this.config.setVerbose(true);
            }
        }
        
        if ( this.config.isVerbose() ) {
            displayConfiguration(this.config);
            Logger.getRootLogger().addAppender(new ConsoleAppender()); 
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
        if ( cfg.getMode().equals(Mode.DIFF)) {
            System.out.println("Collation Configuration: ");
            System.out.println("   Comparand          : " + cfg.getFiles().get(0));
            System.out.println("   Comparand          : " + cfg.getFiles().get(1));
            System.out.println("   Ignore Case        : " + cfg.isIgnoreCase() );
            System.out.println("   Ignore Punctuation : " + cfg.isIgnorePunctuation());
            System.out.println("   Hyphenation        : " + cfg.getHyphenation());
            System.out.println("   Algorithm          : " + cfg.getAlgorithm());
            System.out.println("   Normalize Encoding : " + cfg.isNormalizeEncoding());
        } else if ( cfg.getMode().equals(Mode.STRIP)){
            System.out.println("Tag Strip Configuration: ");
            System.out.println("   File : " + cfg.getFiles().get(0));
        } if ( cfg.getMode().equals(Mode.VALIDATE)){
            System.out.println("XML Validator Configuration: ");
            System.out.println("   File : " + cfg.getFiles().get(0));
        }
    }
    
    /**
     * Perform transformation / tokenizaton / collation based upon parsed config.
     * Results will be streamed to std:out
     * 
     * @throws FileNotFoundException 
     * @throws EncodingException 
     */
    public void execute() throws DiffException, TagStripException, FileNotFoundException, EncodingException {
        if ( this.config.getMode().equals(Mode.VERSION)) {
            System.out.println("JuxtaCL Version "+this.version);
        } else if (this.config.getMode().equals(Mode.HELP)) {
            displayHelp();
        } else if (this.config.getMode().equals(Mode.VALIDATE)) {
            
            validateXml();
        } else if ( this.config.getMode().equals(Mode.STRIP)){
            // strip XML tags and dump plain text to std::out
            String txtContent = doTagStrip();
            System.out.println(txtContent);
        } else {
            // compare the two files and dump change index to std:out
            float ci = doComparison();
            System.out.println(""+ci);
        }
    }
    
    private void displayHelp() {
        StringBuilder out = new StringBuilder("Usage: juxta [-diff file1 file2 [options]] | [-strip file] | [-validate file]\n");
        out.append("  Options:\n");
        out.append("    (+|-)punct                     : Toggle punctuation filtering\n");
        out.append("                                       defaults to ignore punctuation\n");  
        out.append("    (+|-)caps                      : Toggle case sensitivity\n");
        out.append("                                       defaults to case insensitive\n");  
        out.append("    -hyphen (all|linebreak|none)   : Hyphenation inclusion setting\n");
        out.append("                                       defaults to include all\n");  
        out.append("    -algorithm \n");
        out.append("     (juxta|levenshtein|\n");
        out.append("      jaro_winkler)                : Algorthm used to determine change index\n");
        out.append("                                       defaults to juxta\n");  
        out.append("    -normalize                     : Normalize file encoding to UTF-8\n");
        out.append("    -verbose                       : Show progress details\n");
        System.out.println(out.toString());
    }
    
    private void validateXml() throws FileNotFoundException {
        LOG.info("Validate XML file "+this.config.getFiles().get(0));
        File a = new File(this.config.getFiles().get(0));
        if ( a.exists() == false ) {
            LOG.error("Tag Strip Failed. '"+a+"' does not exist");
            throw new FileNotFoundException(a.getPath());
        }
        
        FileReader r = new FileReader(a);
        if ( XmlUtils.isValidXml(r,true) ) {
            System.out.println("Valid XML");
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
     * @throws EncodingException 
     * @throws DiffException 
     * @throws IOException 
     */
    protected float doComparison() throws FileNotFoundException, EncodingException, DiffException {
        // validate files
        LOG.info("Compare "+this.config.getFiles());
        File[] srcs = new File[2];
        boolean[] isXmlFile = new boolean[2];
        for ( int i=0;i<2;i++) {
            srcs[i] = new File(this.config.getFiles().get(i));
            if ( srcs[i].exists() == false ) {
                LOG.error("Compare Failed. '"+ srcs[i]+"' does not exist");
                throw new FileNotFoundException( srcs[i].getPath());
            }
            isXmlFile[i] = hasXmlExtension(srcs[i]);
        }

        String[] text = new String[2];
        if ( this.config.isNormalizeEncoding() ) {
            LOG.info("Get normalized text streams for sources");
            FileInputStream fis = null;
            InputStreamReader isr = null;
            
            try {
                for ( int i=0; i<2; i++) {
                    LOG.info("Source "+(i+1)+"...");
                    File workFile = createCleanWorkFile( srcs[i] );
                    if ( isXmlFile[i] ) {
                        LOG.info("Extracting flat text content from XML source");
                        text[i] = this.tagStripper.stripTags(workFile); 
                    } else {
                        LOG.info("Load flat text content");
                        fis = new FileInputStream(workFile);
                        isr = new InputStreamReader(fis, "UTF-8");
                        text[i] = IOUtils.toString(isr);
                        IOUtils.closeQuietly(isr);
                    }
                    workFile.delete();
                }
            } catch (Exception e) {
                throw new DiffException("Unable to get source text stream", e);
            } finally {
                IOUtils.closeQuietly(isr);
            }
        } else {
            FileReader fr = null;
            try {
                LOG.info("Get text streams for sources");
                for ( int i=0; i<2; i++) {
                    if ( isXmlFile[i] ) {
                        LOG.info("Extracting flat text content from XML source");
                        text[i] = this.tagStripper.stripTags(srcs[i]); 
                    } else {
                        LOG.info("Load flat text content");
                        fr = new FileReader(srcs[i]);
                        text[i] = IOUtils.toString(fr);
                        IOUtils.closeQuietly(fr);
                    }
                }
            } catch (Exception e) {
                throw new DiffException("Unable to get source text stream", e);
            } finally {
                IOUtils.closeQuietly(fr);
            }
        }

        // tokenize the sources
        this.tokenizer.setConfig(this.config);
        List<List<String>> tokens = new ArrayList<List<String>>();
        List<Long> tokenLen = new ArrayList<Long>();
        for (int i = 0; i < text.length; i++) {
            try {
                this.tokenizer.tokenize( new StringReader(text[i]) );
                tokens.add(this.tokenizer.getTokens());
                tokenLen.add( this.tokenizer.getTokenizedLength()  );
                text[i] = null;
                if ( this.config.isVerbose()) {
                    logTokens(this.tokenizer.getTokens());
                }
            } catch (IOException e ) {
                throw new DiffException("Tokenization failed", e);
            }
        }

        LOG.info("Calculate change index");
        this.diffCollator.setAlgorithm(this.config.getAlgorithm());
        return this.diffCollator.diff(tokens.get(0), tokens.get(1), tokenLen.get(0), tokenLen.get(1));
    }
    
    private boolean hasXmlExtension(File file) {
        String path = file.getAbsolutePath().toLowerCase();
        int idx = path.lastIndexOf('.');
        if ( idx > -1 ) {
            String ext = path.substring(idx);
            return ext.equals(".xml");
        }
        return false;
    }

    private void logTokens(List<String> tokens) {
        LOG.info("Source Tokens");
        LOG.info("=============");
        for ( String t : tokens ) {
            LOG.info(t);
        }
        LOG.info("=============\n");
    }

    /**
     * Strip all XML tags and return plain text string
     * 
     * @return
     * @throws TagStripException 
     * @throws FileNotFoundException 
     * @throws EncodingException 
     */
    protected String doTagStrip() throws TagStripException, FileNotFoundException, EncodingException {
        LOG.info("Strip XML tags from "+this.config.getFiles().get(0));
        File a = new File(this.config.getFiles().get(0));
        if ( a.exists() == false ) {
            LOG.error("Tag Strip Failed. '"+a+"' does not exist");
            throw new FileNotFoundException(a.getPath());
        }
        
        LOG.info("Normalizing input file encoding");
        File workFile = createCleanWorkFile(a);
        
        try {
            LOG.info("Extracting flat text content");
            return this.tagStripper.stripTags(workFile);
        } catch ( Exception e ) {
            throw new TagStripException(e);
        }
    }
    
    /**
     * Normalize encoding to UTF-8 and clone the source file into a temporary working
     * file that the JuxtaCL will manipulate during processing.
     * 
     * @param src
     * @return
     * @throws EncodingException
     */
    private File createCleanWorkFile( File src ) throws EncodingException {
        try {
            FileInputStream fis = new FileInputStream(src);
            File workFile = EncodingUtils.fixEncoding(fis);
            IOUtils.closeQuietly(fis);
            workFile.deleteOnExit();
            return workFile;
        } catch (IOException e ) {
            throw new EncodingException(e);
        }
    }
}
