package au.gov.nla.imageanalysis.logic;


import org.json.JSONObject;

/**
 * ImageLabel is the class to store label and confidence paris returned from cloud services
 */
public class ImageLabel {
    private final String name;
    private Float confidence;

    /**  constructors */
    public ImageLabel(String name, Float confidence){this.name = name;this.confidence =confidence;}
    public ImageLabel(String name){this.name = name;this.confidence =1.0f;}

    public String getName() {return name;}
    public float getConfidence(){return confidence;}

    public void setConfidence(float confidence){this.confidence = confidence;}
    public JSONObject toJSON(){
        return new JSONObject().put("label", this.name).put("relevance",this.confidence);
    }
}
