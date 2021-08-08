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
    String testTargetOriginalTitle;
    List<ImageLabel> testTarget;

    /** Constructors  */
    public ServiceOutput(String pid, List<ServiceType> serviceTypes){
        this.services = new HashMap<>();
        this.pid = pid;
        this.testTarget = new ArrayList<>();
        this.testTargetOriginalTitle = "";
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
    public String[] toCsv(){
        int numOfServiceType = ServiceType.values().length;
        String [] csvFormat = new String[numOfServiceType*2+3];
        Arrays.fill(csvFormat, "");
        csvFormat[0] = pid;
        csvFormat[1] = this.testTargetOriginalTitle;
        csvFormat[2] = testTargetToCsv();
        for(ServiceType serviceType: this.services.keySet()){
            csvFormat[serviceType.getId()*2+1] = this.services.get(serviceType).toCsv();
            if(testTarget.size()>0){
                Float evaluationScore= this.services.get(serviceType).getEvaluationScore();
                csvFormat[serviceType.getId()*2+2] = evaluationScore==null?"":evaluationScore+"";
            }
        }
        return csvFormat;
    }


    /**
     * The method builds the title row of csv file
     */
    public String[] makeCsvTitles(){
        int numOfServiceType = ServiceType.values().length;
        String [] csvTitle = new String[numOfServiceType*2+3];
        Arrays.fill(csvTitle, "");
        csvTitle[0] = "pid";
        csvTitle[1] = "Original Title Description";
        csvTitle[2] = "Labels from Original Title Description";
        for(ServiceType serviceType: ServiceType.values()){
            csvTitle[serviceType.getId()*2+1] = serviceType.getDescription()+" Result";
            csvTitle[serviceType.getId()*2+2] = serviceType.getDescription()+" Evaluation Score (0-1)";
        }
        return csvTitle;
    }

    /**
     * The method generates String to be written into the Test Labels column of the csv file
     */
    public String testTargetToCsv(){
        String csvFormat = "";
        for(int i=0;i<testTarget.size();i++){
            if(i!=0){
                csvFormat+=", ";
            }
            csvFormat+=testTarget.get(i).toCsv();
        }
        return csvFormat;
    }

    /**
     * The method loads the test target from the test label file
     */
    public void loadTestTargets(String filePath) throws IOException {
        List<String[]> list = InOut.csvReaderOneByOne(filePath);
        for(int i=1; i<list.size();i++){
            String testPid = list.get(i)[0];
            if(testPid.equals(pid)){
                this.testTargetOriginalTitle = list.get(i)[1];
                String [] labels = list.get(i)[2].split(" ");
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
