package org.juxtasoftware;

import java.util.List;

import org.apache.log4j.Logger;
import org.juxtasoftware.model.Configuration.Algorithm;

import scala.Option;

import com.rockymadden.stringmetric.similarity.LevenshteinMetric;

import difflib.Delta;
import difflib.Delta.TYPE;
import difflib.DiffUtils;
import difflib.Patch;

/**
 * 
 * @author loufoster
 *
 */
public class DiffCollator {
    private static Logger LOG = Logger.getLogger(DiffCollator.class);
    private Algorithm algorithm = Algorithm.JUXTA;
    
    /**
     * Set the algorithm used to calculate the percentage difference
     * @param algo
     */
    public void setAlgorithm(Algorithm algo) {
        this.algorithm = algo;
    }
    
    /**
     * Compare the token streams of two documents and return a floating point value
     * between 0.0 and 1.0 that is an indicator of how different the two streams are.
     * Value of 0 means that there is no difference, 1 means completely different.
     * 
     * @return
     */
    public float diff(List<String> tokensA, List<String> tokensB) {
        LOG.info("Diff the texts");
        long lengthA = getSourceLength(tokensA);
        long lengthB = getSourceLength(tokensB);
        Patch diffResult = DiffUtils.diff(tokensA, tokensB);
        
        switch ( this.algorithm ) {
            case JUXTA:
                return calcJuxtaChangeIndex( diffResult.getDeltas(), lengthA, lengthB );
            case LEVENSHTEIN:
                return calcLevenshteinDifference( diffResult.getDeltas(), lengthA, lengthB );
            default:
                throw new RuntimeException(this.algorithm+" is not yet supported");
        }
    }

    /**
     * calculate the percent differnece between the texts based in levenshtein distance
     * @param deltas
     * @param lengthB 
     * @param lengthA 
     * @return
     */
    private float calcLevenshteinDifference(List<Delta> deltas, long lengthA, long lengthB) {
        LOG.info("Compute percentage difference using levenshtein distances");
        int levSum = 0;
        int delSum = 0;
        int addSum = 0;
        for ( Delta delta : deltas ) {
            if ( delta.getType().equals(TYPE.CHANGE)) {
                for (int i=0; i<delta.getOriginal().getLines().size(); i++) {
                    String orig = (String)delta.getOriginal().getLines().get(i);
                    String rev = (String)delta.getRevised().getLines().get(i);
                    Option<Object> out = LevenshteinMetric.apply().compare(orig, rev, null);
                    Integer val = (Integer)out.get();
                    levSum+= val;
                }
            } else if ( delta.getType().equals(TYPE.DELETE)) {
                // text was deleted from A. get the deleted tokens
                for ( Object delObj : delta.getOriginal().getLines() ) {
                    String delToken = (String)delObj;
                    delSum += delToken.length();
                }
            } else {
                // text was inserted relative to A. get the text from B
                for ( Object insObj : delta.getRevised().getLines() ) {
                    String insToken = (String)insObj;
                    addSum += insToken.length();
                }
            }
        }
        
        return (float)(levSum+addSum-delSum) / (float)Math.max(lengthA, lengthB);
    }

    /**
     * calculate the change index using the algorithm found in JuxtaDesktop and JuxtaWS
     * 
     * @param deltas
     * @param lengthB 
     * @param lengthA 
     * @return
     */
    private float calcJuxtaChangeIndex(List<Delta> deltas, long lengthA, long lengthB) {
        LOG.info("Compute change index using Juxa algorithm");
        long diffSum=0;
        for ( Delta delta : deltas ) {
            if ( delta.getType().equals(TYPE.CHANGE)) {
                for (int i=0; i<delta.getOriginal().getLines().size(); i++) {
                    String orig = (String)delta.getOriginal().getLines().get(i);
                    String rev = (String)delta.getRevised().getLines().get(i);
                    diffSum += Math.max(orig.length(), rev.length());
                }
            } else if ( delta.getType().equals(TYPE.DELETE)) {
                // text was deleted from A. get the deleted tokens
                for ( Object delObj : delta.getOriginal().getLines() ) {
                    String delToken = (String)delObj;
                    diffSum += delToken.length();
                }
            } else {
                // text was inserted relative to A. get the text from B
                for ( Object insObj : delta.getRevised().getLines() ) {
                    String insToken = (String)insObj;
                    diffSum += insToken.length();
                }
            }
        }
        return (float)(diffSum) / (float)Math.max(lengthA, lengthB);
    }
    
    private long getSourceLength( List<String> srcTokens ) {
        long len = 0;
        for ( String t : srcTokens ) {
            len += t.length();
        }
        return len;
    }
}
