package au.gov.nla.imageanalysis.util;

import au.gov.nla.imageanalysis.enums.ServiceType;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class ImageLabel {
    private String name;
    private Float confidence;

    public ImageLabel(String name, Float relavence){
        this.name = name;
        this.confidence =relavence;
    }

    public static JSONObject covertToJSON(List<ImageLabel> serviceOutput, ServiceType serviceType){
        if(serviceOutput.size()!=0){
            List<JSONObject> labelsAsJsonObject = new ArrayList<>();
            for (ImageLabel imageLabel: serviceOutput){
                labelsAsJsonObject.add(new JSONObject().put("label",imageLabel.name).put("relevance",imageLabel.confidence));
            }
            return new JSONObject().put("id",serviceType.getCode()).put("labels",new JSONArray(labelsAsJsonObject));
        }else {
            return new JSONObject().put("id",serviceType.getCode()).put("labels","No labels detected");
        }
    }







    public String getName() {return name; }
    public Float getRelevence() {return confidence;}
}
