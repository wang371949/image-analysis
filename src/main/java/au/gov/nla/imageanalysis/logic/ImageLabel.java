package au.gov.nla.imageanalysis.logic;


import org.json.JSONObject;

/**
 * ImageLabel is the class to store label and confidence paris returned from cloud services
 */
public class ImageLabel {
    private final String name;
    private final Float confidence;

    /**  constructors */
    public ImageLabel(String name, Float confidence){this.name = name.toLowerCase();this.confidence =confidence;}
    public ImageLabel(String name){this.name = name.toLowerCase();this.confidence =-1.0f;}

    public String getName() {return name;}
    public float getConfidence(){return confidence;}


    /**
     * The method converts the fields variables to a JSON object
     */
    public JSONObject toJSON(){
        return new JSONObject().put("label", this.name).put("relevance",this.confidence);
    }

    /**
     * The method converts the fields variables to String to be written into csv file
     */
    public String toCustomizedCsvFormat(){
        return confidence==-1.0f?name:"("+name+":"+confidence+")";
    }
}
