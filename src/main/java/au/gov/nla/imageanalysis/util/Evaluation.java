package au.gov.nla.imageanalysis.util;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class Evaluation {
    /**
     * This method calculate the evaluation score. Further improvement may be required
     * @param serviceOutput outputs from cloud service API functions.
     * @param targets targets read from csv file
     * @return evaluation score
     */
    public static float getEvaluation (List<ImageLabel> serviceOutput, List<ImageLabel> targets){
        float evaluationScore = 0.0f;
        for(ImageLabel outputLabel: softmax(serviceOutput)){
            for(ImageLabel targetLabel: targets){
                if (outputLabel.getName().equals(targetLabel.getName())){
                    evaluationScore += outputLabel.getConfidence();
                }
            }
        }
        return evaluationScore;
    }

    /**
     * This method converts the distribution of output confidence values into softmax distribution.
     * It forms part of evaluation calculation
     * @param serviceOutput outputs from cloud service API functions.
     * @return same list but with confidence values in softmax distribution
     */
    public static List<ImageLabel> softmax(List<ImageLabel> serviceOutput){
        double [] confidences = new double[serviceOutput.size()];
        for (int i=0;i<serviceOutput.size();i++){confidences[i] = serviceOutput.get(i).getConfidence();}
        float total =(float) Arrays.stream(confidences).map(Math::exp).sum();
        for (int i=0; i<serviceOutput.size();i++){
            serviceOutput.set(i, new ImageLabel(serviceOutput.get(i).getName(),
                    ((float) Math.exp((serviceOutput.get(i).getConfidence()))/total)));
        }
        return serviceOutput;
    }

    /**
     * This method breaks down a sentence label into separate word labels. It allows the evaluation to take place
     * on word by word basis.
     * @param sentenceOutput list of imageLabel instances with labels as sentences
     * @return outputs from cloud service API functions
     */
    public static List<ImageLabel> fromSentencesToLabels (List<ImageLabel> sentenceOutput){
        List<ImageLabel>imageLabels = new ArrayList<>();
        for(ImageLabel sentenceLabel: sentenceOutput) {
            String [] labels = sentenceLabel.getName().split(" ");
            for(String label: labels) {
                imageLabels.add(new ImageLabel(label,sentenceLabel.getConfidence()));
            }
        }
        return imageLabels;
    }
}
