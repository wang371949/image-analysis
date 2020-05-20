package au.gov.nla.imageanalysis.util;

import au.gov.nla.imageanalysis.enums.ServiceType;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ImageLabel {
    private String name;
    private Float confidence;

    /**  constructors */
    public ImageLabel(String name, Float confidence){this.name = name;this.confidence =confidence;}
    public ImageLabel(String name){this.name = name;this.confidence =1.0f;}

    /**
     * This method processes the results returned from cloud services, evalute them and store them in a JSON object.
     * @param serviceOutput outputs from cloud service API functions.
     * @param serviceType an enum indicate the type of cloud service
     * @param pid the id of the pictrue being processed
     * @return a JSON object of pre-defined format
     */
    public static JSONObject covertToJSON(List<ImageLabel> serviceOutput, ServiceType serviceType, String pid){
        if(serviceOutput.size()!=0){
            List<JSONObject> labelsAsJsonObject = new ArrayList<>();
            for (ImageLabel imageLabel: serviceOutput){
                labelsAsJsonObject.add(new JSONObject().put("label",imageLabel.name).put("relevance",imageLabel.confidence));
            }
            Map<String,List<ImageLabel>> targets = CSVHelper.readCSV("src/main/resources/static/Image Labels/labels.csv");
            if (targets.keySet().contains(pid)){
                serviceOutput = fromSentencesToLabels(serviceOutput);
                float evaluationScore = evaluation(serviceOutput,targets.get(pid));
                return new JSONObject().put("id",serviceType.getCode()).put("labels",new JSONArray(labelsAsJsonObject))
                        .put("Evaluation", evaluationScore);
            }else{
                return new JSONObject().put("id",serviceType.getCode()).put("labels",new JSONArray(labelsAsJsonObject))
                        .put("Evaluation", "Not in Test Set");
            }
        }
        return new JSONObject().put("id",serviceType.getCode()).put("labels","No labels detected");
    }

    /**
     * This method calculate the evaluation score. Further improvement may be required
     * @param serviceOutput outputs from cloud service API functions.
     * @param targets targets read from csv file
     * @return evaluation score
     */
    private static float evaluation (List<ImageLabel> serviceOutput, List<ImageLabel> targets){
        float evaluationScore = 0.0f;
        for(ImageLabel outputLabel: softmax(serviceOutput)){
            for(ImageLabel targetLabel: targets){
                if (outputLabel.name.equals(targetLabel.name)){
                    evaluationScore += outputLabel.confidence;
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
    private static List<ImageLabel> softmax(List<ImageLabel> serviceOutput){
        double [] confidences = new double[serviceOutput.size()];
        for (int i=0;i<serviceOutput.size();i++){confidences[i] = serviceOutput.get(i).confidence;}
        float total =(float) Arrays.stream(confidences).map(Math::exp).sum();
        for (int i=0; i<serviceOutput.size();i++){
            serviceOutput.set(i, new ImageLabel(serviceOutput.get(i).name,
                    ((float) Math.exp((serviceOutput.get(i).confidence))/total)));
        }
        return serviceOutput;
    }

    /**
     * This method breaks down a sentence label into separate word labels. It allows the evaluation to take place
     * on word by word basis.
     * @param sentenceOutput list of imageLabel instances with labels as sentences
     * @return outputs from cloud service API functions
     */
    private static List<ImageLabel> fromSentencesToLabels (List<ImageLabel> sentenceOutput){
        List<ImageLabel>imageLabels = new ArrayList<>();
        for(ImageLabel sentenceLabel: sentenceOutput) {
            String [] labels = sentenceLabel.name.split(" ");
            for(String label: labels) {
                imageLabels.add(new ImageLabel(label,sentenceLabel.confidence));
            }
        }
        return imageLabels;
    }

    public String getName() {return name; }
    public Float getRelevence() {return confidence;}
}
