package au.gov.nla.imageanalysis.logic;



import au.gov.nla.imageanalysis.enums.ServiceType;
import au.gov.nla.imageanalysis.util.Maths;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * ImageLabels is a class to store a collection of labels returned from a single cloud service
 */
public class ImageLabels {
    Float evaluationScore;
    ServiceType serviceType;
    List<ImageLabel> imageLabels = new ArrayList<>();

    /** Constructors */
    public ImageLabels(ServiceType serviceType){
        this.serviceType = serviceType;
    }

    public void addImageLabel(ImageLabel imageLabel){
        imageLabels.add(imageLabel);
    }

    /**
     * The method converts the fields variables to a JSON object
     */
    public JSONObject toJSON(){
        JSONObject output = new JSONObject();
        List<JSONObject> labelsAsJsonObject = new ArrayList<>();
        for(ImageLabel imageLabel:imageLabels){
            labelsAsJsonObject.add(imageLabel.toJSON());
        }
        output.put("id", serviceType.getCode());
        if(evaluationScore!=null) {
            output.put("Evaluation Score",evaluationScore);
        }
        output.put("labels", new JSONArray(labelsAsJsonObject));
        return output;
    }

    /**
     * This method calculate the evaluation score.
     * @param target the list of target labels of a given pid in the test file
     */
    public void getEvaluation (List<ImageLabel> target){
        List<ImageLabel> preprocessedLabels = this.serviceType.getLabelAreSentences()? softmaxOperation(fromSentencesToLabels()):imageLabels;
        float score = 0.0f;
        for (ImageLabel imageLabel : preprocessedLabels) {
            for (ImageLabel targetLabel : target) {
                if (imageLabel.getName().equals(targetLabel.getName())) {
                    score += imageLabel.getConfidence();
                }
            }
        }
        this.evaluationScore = score;
    }

    /**
     * This method converts the distribution of output confidence values into softmax distribution.
     * It forms part of evaluation calculation
     */
    public List<ImageLabel> softmaxOperation(List<ImageLabel> imageLabelsForProcessing){
        List<ImageLabel>softmaxLabels = new ArrayList<>();
        int numOfLabels = imageLabelsForProcessing.size();
        double [] confidences = new double[numOfLabels];
        for (int i=0; i<numOfLabels;i++){
            confidences[i]= imageLabelsForProcessing.get(i).getConfidence();
        }
        double [] softmaxConfidences = Maths.softmax(confidences);
        for (int i=0; i<numOfLabels;i++){
            softmaxLabels.add(new ImageLabel(imageLabelsForProcessing.get(i).getName(),(float) softmaxConfidences[i]));
        }
        return softmaxLabels;
    }

    /**
     * This method breaks down a sentence label into separate word labels. It allows the evaluation to take place
     * on word by word basis.
     */
    public List<ImageLabel> fromSentencesToLabels (){
        List<ImageLabel>separatedLabels = new ArrayList<>();
        for(ImageLabel sentenceLabel: this.imageLabels) {
            String [] labels = sentenceLabel.getName().split(" ");
            for(String label: labels) {
                separatedLabels.add(new ImageLabel(label,sentenceLabel.getConfidence()));
            }
        }
        return separatedLabels;
    }
}
