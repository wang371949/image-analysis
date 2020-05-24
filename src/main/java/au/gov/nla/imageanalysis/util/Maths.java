package au.gov.nla.imageanalysis.util;


import org.apache.commons.text.similarity.JaroWinklerDistance;
import org.apache.commons.text.similarity.JaroWinklerSimilarity;

import java.util.Arrays;

public class Maths {

    /**
     * This method converts the distribution in probability array into softmax distribution.
     * It forms part of evaluation calculation
     * @param probabilities probabilities distribution array that needs to be converted.
     * @return probability distribution array in softmax distribution
     */
    public static double[] softmax(double[] probabilities){
        double total =Arrays.stream(probabilities).map(Math::exp).sum();
        for (int i=0; i<probabilities.length;i++){
            probabilities[i] = Math.exp(probabilities[i])/total;
        }
        return probabilities;
    }

    public static double wordSimilarity(String word1, String word2){
        JaroWinklerSimilarity jaroWinklerSimilarity = new JaroWinklerSimilarity();
        return jaroWinklerSimilarity.apply(word1,word2);
    }
}
