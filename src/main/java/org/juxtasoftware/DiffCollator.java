package org.juxtasoftware;

import java.util.List;

import org.apache.log4j.Logger;
import org.juxtasoftware.model.Configuration.Algorithm;

import scala.Option;

import com.rockymadden.stringmetric.similarity.DiceSorensenMetric;
import com.rockymadden.stringmetric.similarity.JaroWinklerMetric;
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
            case JARO_WINKLER:
            case DICE_SORENSEN:
                return calcSimilarityMetric( diffResult.getDeltas(), tokensA.size() );
            default:
                throw new RuntimeException(this.algorithm+" is not yet supported");
        }
    }

    /**
     * calculate the percent difference between the texts based in jaro-winkler distance
     * note: jaro result is from 0-1, with 0 being completely different and 1 being the same
     * 
     * @param deltas
     * @param lengthB 
     * @param lengthA 
     * @return
     */
    private float calcSimilarityMetric(List<Delta> deltas, long baseTokenCnt ) {
        LOG.info("Compute percentage difference using "+this.algorithm+" similarity metric");
        float jaro = 0f;
        int cnt = 0;
        int baseTokenIndex = 0; 
        int witnessTokenIndex = 0;
        for (Delta delta : deltas) {
            
            // grab references to diff token indexes for base (original) and witness (revised)
            int baseDiffTokenStartIndex = delta.getOriginal().getPosition();
            int baseDiffTokenEndIndex = baseDiffTokenStartIndex + delta.getOriginal().getLines().size();
            int witnessDiffTokenStartIndex = delta.getRevised().getPosition();
            int witnessDiffTokenEndIndex = witnessDiffTokenStartIndex + delta.getRevised().getLines().size();
            
            do {
                // curr indexes are before change - these are aligned and exact matches
                if ( baseTokenIndex < baseDiffTokenStartIndex && witnessTokenIndex < witnessDiffTokenStartIndex) {
                    baseTokenIndex++;
                    witnessTokenIndex++;
                    jaro += 1f;
                    cnt++;
                    continue;
                }

                // curr indexes are both within change bounds; this is a difference. calc similarity
                if ( baseTokenIndex < baseDiffTokenEndIndex && witnessTokenIndex < witnessDiffTokenEndIndex) {
                    String orig = (String) delta.getOriginal().getLines().get(baseTokenIndex-baseDiffTokenStartIndex);
                    String rev = (String) delta.getRevised().getLines().get(witnessTokenIndex-witnessDiffTokenStartIndex);
                    baseTokenIndex++;
                    witnessTokenIndex++;
                    Option<Object> out = null;
                    if ( this.algorithm.equals(Algorithm.JARO_WINKLER) ) {
                        out = JaroWinklerMetric.apply().compare(orig, rev, null);
                    } else {
                        out = DiceSorensenMetric.apply().compare(orig, rev, 1);
                    }
                    Double val = (Double) out.get();
                    jaro += val.floatValue();
                    cnt++;
                    continue;
                }
                
                // Base still within change range, witnesss not. 
                if ( baseTokenIndex < baseDiffTokenEndIndex && witnessTokenIndex >= witnessDiffTokenEndIndex) {
                    baseTokenIndex++;
                    cnt++;
                    continue;
                }
                
                // WITNESS still within change range, base not.
                if ( baseTokenIndex >= baseDiffTokenEndIndex && witnessTokenIndex < witnessDiffTokenEndIndex) {
                    witnessTokenIndex++;
                    cnt++;
                    continue;
                }
                
                
            } while ( baseTokenIndex <  baseDiffTokenEndIndex || witnessTokenIndex <  witnessDiffTokenEndIndex );
            
        }
        
        // all tokens after the last diff index align and are the same.
        // add them to the running average
        jaro = jaro + (baseTokenCnt - baseTokenIndex);
        cnt = (int) (cnt + (baseTokenCnt - baseTokenIndex));
        
        // jaro values are opposite of expected; 1 is the same instead of totally different.
        // flip it around to match other results
        return 1.0f-(jaro/cnt);
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
                String orig = "";
                for (Object ol : delta.getOriginal().getLines() ) {
                    orig += (String)ol;
                }
                String rev = "";
                for (Object rl : delta.getRevised().getLines() ) {
                    rev += (String)rl;
                }
                Option<Object> out = LevenshteinMetric.apply().compare(orig, rev, null);
                Integer val = (Integer)out.get();
                levSum+= val;
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
        
        return normalizeResult( (float)(levSum+addSum+delSum) / (float)Math.max(lengthA, lengthB) );
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
        long addSum=0;
        long delSum=0;
        for ( Delta delta : deltas ) {
            if ( delta.getType().equals(TYPE.CHANGE)) {
                int origLen = 0;
                for (Object ol : delta.getOriginal().getLines() ) {
                    origLen += ((String)ol).length();
                }
                int revLen = 0;
                for (Object rl : delta.getRevised().getLines() ) {
                    revLen += ((String)rl).length();
                }
                diffSum += Math.max(origLen, revLen); 
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
        
        return normalizeResult( (float)(diffSum+addSum+delSum) / (float)Math.max(lengthA, lengthB) );
    }
    
    private float normalizeResult(float ci) {
        ci = Math.min(1f, ci);
        ci = Math.max(ci, 0f);
        return ci;
    }
    
    private long getSourceLength( List<String> srcTokens ) {
        long len = 0;
        for ( String t : srcTokens ) {
            len += t.length();
        }
        return len;
    }
}
