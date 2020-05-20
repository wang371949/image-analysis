package au.gov.nla.imageanalysis.service;


import au.gov.nla.imageanalysis.config.ApplicationConfiguration;
import au.gov.nla.imageanalysis.enums.ServiceType;
import au.gov.nla.imageanalysis.util.CSVHelper;
import au.gov.nla.imageanalysis.util.Evaluation;
import au.gov.nla.imageanalysis.util.HttpHelper;
import au.gov.nla.imageanalysis.util.ImageLabel;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class ImageService {
    private final Logger log = LoggerFactory.getLogger(ImageService.class);
    @Autowired private ApplicationConfiguration config;
    @Autowired private AWSImageService awsImageService;
    @Autowired private GoogleImageService googleImageService;
    @Autowired private AzureImageService azureImageService;

    /** Constructor */
    public ImageService(){ super(); }

    /*
     * The access to the library computer is not available, so it uses urlReplacement, a online url for trove image access,
     * to create imageService. This is only for testing the functionality of the cloud APIs. Once in the office environment,
     * urlCorrect, should be used to create imageService.
     */
    private String getUrl(String pid){
        String urlCorrect = config.getCorrectUrl()+pid;
        String urlReplacement = config.getReplacementUrl();
        log.info("Correct Url: {}",urlCorrect);
        log.info("Replacement URL: {}", urlReplacement);
        return urlReplacement;
    }

    /**
     * Capture the image from given url and stored as an inputStream
     * @return an inputStream of an image from a given url
     * @throws IOException if the url contents cannot be retrieved
     */
    public InputStream getInputStreamFromUrl(String url) throws IOException{ return HttpHelper.getAsStream(url);}

    /**
     * call Amazon Web Service (AWS) image Labeling API
     * @param pid the id of the image that's being processed by Google Cloud Vision.
     * @return a JSONObject containing results from AWS in the required format
     *         eg, {"id","AL", "labels":[{"label":"Photograph", "relevance": 0.9539},...]}
     */
    public JSONObject AWSImageLabeling(String pid){
        return covertToJSON(awsImageService.AWSImageLabeling(getUrl(pid), config.getAWSAccessKey(),config.getAWSSecretKey()),
                ServiceType.AL,
                pid);}

    /**
     * Call google cloud vision image Labeling API
     * @param pid the id of the image that's being processed by Google Cloud Vision.
     * @return a JSONObject containing results from google cloud vision in the required format
     *         eg, {"id","GL", "labels":[{"label":"Photograph", "relevance": 0.9539},...]}
     */
    public JSONObject googleImageLabeling(String pid){
        return covertToJSON(googleImageService.googleImageLabeling(getUrl(pid)),
                ServiceType.GL,
                pid);}

    /**
     * Call microsoft azure computer vision image Labeling API
     * @param pid the id of the image that's being processed by Microsoft azure.
     * @return a JSONObject containing results from microsoft azure  in the required format
     *        eg, {"id","ML", "labels":[{"label":"Photograph", "relevance": 0.9539},...]}
     */
    public JSONObject azureImageLabeling(String pid){
        return covertToJSON(azureImageService.azureImageLabeling(getUrl(pid),config.getAzureAccessKey(),config.getAzureEndPoint()),
                ServiceType.ML,
                pid);}

    /**
     * Call microsoft azure computer vision image description API
     * @param pid the id of the image that's being processed by Microsoft azure.
     * @return a JSONObject containing results from microsoft azure  in the required format
     *        eg, {"id","MD", "descriptions":[{"description":"Photograph", "relevance": 0.9539},...]}
     */
    public JSONObject azureImageDescription(String pid){
        return covertToJSON(azureImageService.azureImageDescription(getUrl(pid),config.getAzureAccessKey(),config.getAzureEndPoint()),
                ServiceType.MD,
                pid);}

    /**
     * save the image of given url into local file for visualization
     * @param pid the id of the image that's being saved.
     */
    public void saveImage(String pid){
        BufferedImage image = null;
        File outputfile = new File(config.getImageSaveLocation());
        try{
            InputStream in = getInputStreamFromUrl(getUrl(pid));
            image = ImageIO.read(in);
            ImageIO.write(image,"jpg",outputfile);
        } catch (IOException e){log.error("IOException: "+e.getMessage(),e);}
    }


    /**
     * This method processes the results returned from cloud services, evalute them and store them in a JSON object.
     * @param serviceOutput outputs from cloud service API functions.
     * @param serviceType an enum indicate the type of cloud service
     * @param pid the id of the pictrue being processed
     * @return a JSON object of pre-defined format
     */
    private JSONObject covertToJSON(List<ImageLabel> serviceOutput, ServiceType serviceType, String pid){
        if(serviceOutput.size()!=0){
            List<JSONObject> labelsAsJsonObject = new ArrayList<>();
            for (ImageLabel imageLabel: serviceOutput){
                labelsAsJsonObject.add(new JSONObject().put("label",imageLabel.getName()).put("relevance",imageLabel.getConfidence()));
            }
            Map<String,List<ImageLabel>> targets = CSVHelper.readCSV(config.getTestLabelLocation());
            if (targets.containsKey(pid)){
                serviceOutput = Evaluation.fromSentencesToLabels(serviceOutput);
                float evaluationScore = Evaluation.getEvaluation(serviceOutput,targets.get(pid));
                return new JSONObject().put("id",serviceType.getCode()).put("labels",new JSONArray(labelsAsJsonObject))
                        .put("Evaluation", evaluationScore);
            }else{
                return new JSONObject().put("id",serviceType.getCode()).put("labels",new JSONArray(labelsAsJsonObject))
                        .put("Evaluation", "Not in Test Set");
            }
        }
        return new JSONObject().put("id",serviceType.getCode()).put("labels","No labels detected");
    }
}
