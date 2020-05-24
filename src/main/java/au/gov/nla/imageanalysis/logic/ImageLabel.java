package au.gov.nla.imageanalysis.logic;


import au.gov.nla.imageanalysis.service.ImageService;
import au.gov.nla.imageanalysis.util.Maths;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ImageLabel is the class to store label and confidence paris returned from cloud services
 */
public class ImageLabel {
    private final String name;
    private final Float confidence;

    /**  constructors */
    public ImageLabel(String name, Float confidence){this.name = name.toLowerCase();this.confidence =(float)(Math.round(confidence*100.0)/100.0);}
    public ImageLabel(String name){this.name = name.toLowerCase();this.confidence =-1.0f;}

    public String getName() {return name;}
    public float getConfidence(){return confidence;}


    public boolean isSimilar(ImageLabel other, float threshold){
        return Maths.wordSimilarity(this.name,other.name)>threshold;
    }


    /**
     * The method converts the fields variables to a JSON object
     */
    public JSONObject toJSON(){
        return new JSONObject().put("label", this.name).put("relevance",this.confidence);
    }

    /**
     * The method converts the fields variables to String to be written into csv file
     */
    public String toCsv(){
        return confidence==-1.0f?name:"("+name+":"+confidence+")";
    }
}
