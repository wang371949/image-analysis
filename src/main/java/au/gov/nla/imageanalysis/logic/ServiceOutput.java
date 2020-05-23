package au.gov.nla.imageanalysis.logic;

import au.gov.nla.imageanalysis.enums.ServiceType;
import au.gov.nla.imageanalysis.service.ImageService;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ServiceOutput is a class to store the entire service output of a given pid from multiple cloud services
 */
public class ServiceOutput {
    private final Logger log = LoggerFactory.getLogger(ImageService.class);
    Map<ServiceType,ImageLabels> service;
    String pid;
    public ServiceOutput(String pid){
        this.service = new HashMap<>();
        this.pid = pid;
    }

    public void putImageServiceResult(ServiceType serviceType, ImageLabels imageLabels){
        this.service.put(serviceType,imageLabels);
    }


    /**
     * The method check if an image id is presented in the test file
     * @param pid the pictrue id of the image being processed
     * @param targets The targets extracted from the text file
     * @return
     */
    public boolean isInTheTestSet(String pid, Map<String,List<ImageLabel>> targets){
        if(targets.containsKey(pid)){
            return true;
        }else {
            log.info("pid: {}, is not in the test file and not evaluated",pid);
            return false;
        }
    }

    /**
     * The method converts the fields variables to a JSON object
     */
    public JSONObject toJSON(){
        List<JSONObject> labelsAsJsonObject = new ArrayList<>();
        for(ServiceType serviceType: this.service.keySet()){
            labelsAsJsonObject.add(this.service.get(serviceType).toJSON());
        }
        return new JSONObject().put("pid", pid).put("service", new JSONArray(labelsAsJsonObject));
    }
}
