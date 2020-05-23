package au.gov.nla.imageanalysis.logic;

import au.gov.nla.imageanalysis.enums.ServiceType;
import au.gov.nla.imageanalysis.util.InOut;
import org.json.JSONArray;
import org.json.JSONObject;


import java.io.IOException;
import java.util.*;

/**
 * ServiceOutput is a class to store the entire service output of a given pid from multiple cloud services
 */
public class ServiceOutput {
    Map<ServiceType,ImageLabels> services;
    List<ServiceType> serviceTypes;
    String pid;
    List<ImageLabel> testTarget;

    /** Constructors  */
    public ServiceOutput(String pid, List<ServiceType> serviceTypes){
        this.services = new HashMap<>();
        this.pid = pid;
        testTarget = new ArrayList<>();
        this.serviceTypes = serviceTypes;
    }
    public ServiceOutput(List<ServiceType> serviceTypes){
        this.services = new HashMap<>();
        this.pid = "";
        testTarget = new ArrayList<>();
        this.serviceTypes = serviceTypes;
    }

    public void putImageServiceResult(ServiceType serviceType, ImageLabels imageLabels){
        this.services.put(serviceType,imageLabels);
    }


    /**
     * The method converts the fields variables to a JSON object
     */
    public JSONObject toJSON(){
        List<JSONObject> labelsAsJsonObject = new ArrayList<>();
        for(ServiceType serviceType: this.services.keySet()){
            labelsAsJsonObject.add(this.services.get(serviceType).toJSON());
        }
        return new JSONObject().put("pid", pid).put("service", new JSONArray(labelsAsJsonObject));
    }

    /**
     * The method converts the fields variables to a String array which will be written into a csv file
     */
    public String[] toCustomizedCsvFormat(){
        int numOfServiceType = ServiceType.values().length;
        String [] customizedCsvFormat = new String[numOfServiceType*2+2];
        Arrays.fill(customizedCsvFormat, "");
        customizedCsvFormat[0] = pid;
        customizedCsvFormat[1] = testTargetToCustomizedCsvFormat();
        for(ServiceType serviceType: this.services.keySet()){
            customizedCsvFormat[serviceType.getId()*2] = this.services.get(serviceType).toCustomizedCsvFormat();
            if(testTarget.size()>0){
                Float evaluationScore= this.services.get(serviceType).getEvaluationScore();
                customizedCsvFormat[serviceType.getId()*2+1] = evaluationScore==null?"":evaluationScore+"";
            }
        }
        return customizedCsvFormat;
    }


    /**
     * The method builds the title row of csv file
     */
    public String[] csvTitles(){
        int numOfServiceType = ServiceType.values().length;
        String [] customizedCsvTitle = new String[numOfServiceType*2+2];
        Arrays.fill(customizedCsvTitle, "");
        customizedCsvTitle[0] = "pid";
        customizedCsvTitle[1] = "Test Labels";
        for(ServiceType serviceType: serviceTypes){
            customizedCsvTitle[serviceType.getId()*2] = serviceType.getDescription()+" Result";
            customizedCsvTitle[serviceType.getId()*2+1] = serviceType.getDescription()+" Evaluation Score";
        }
        return customizedCsvTitle;
    }

    /**
     * The method generates String to be written into the Test Labels column of the csv file
     */
    public String testTargetToCustomizedCsvFormat(){
        String customizedCsvFormat = "";
        for(int i=0;i<testTarget.size();i++){
            if(i!=0){
                customizedCsvFormat+=",";
            }
            customizedCsvFormat+=testTarget.get(i).toCustomizedCsvFormat();
        }
        return customizedCsvFormat;
    }

    /**
     * The method loads the test target from the test label file
     */
    public void loadTestTargets(String filePath) throws IOException {
        List<String[]> list = InOut.csvReaderOneByOne(filePath);
        for(int i=1; i<list.size();i++){
            String testPid = list.get(i)[0];
            if(testPid.equals(pid)){
                String [] labels = list.get(i)[1].split(" ");
                for (String label : labels) {
                    testTarget.add(new ImageLabel(label));
                }
            }
        }
    }

    public List<ImageLabel> getTestTarget() {
        return testTarget;
    }
}
