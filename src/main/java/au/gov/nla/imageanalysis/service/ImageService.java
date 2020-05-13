package au.gov.nla.imageanalysis.service;


import au.gov.nla.imageanalysis.config.ApplicationConfiguration;
import au.gov.nla.imageanalysis.util.HttpHelper;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.rekognition.model.*;
import com.amazonaws.util.IOUtils;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.EntityAnnotation;
import com.google.cloud.vision.v1.Feature;
import com.microsoft.azure.cognitiveservices.vision.computervision.*;
import com.microsoft.azure.cognitiveservices.vision.computervision.models.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gcp.vision.CloudVisionTemplate;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.List;

@Component
public class ImageService {

    private final Logger log = LoggerFactory.getLogger(ImageService.class);

    @Autowired
    private ApplicationConfiguration config;

    /**
     * Strategy interface for loading resources. Its getResource method uses an image url to return
     * a Resource instance containing the image ByteString.
     */
    @Autowired
    private ResourceLoader resourceLoader;

    /**
     * The CloudVisionTemplate is a wrapper around the Vision API Client Libraries and lets you process images easily
     * through the Vision API. For more information about the CloudVisionTemplate features, see:
     * https://cloud.spring.io/spring-cloud-static/spring-cloud-gcp/1.2.0.RELEASE/reference/html/#google-cloud-vision
     */
    @Autowired
    private CloudVisionTemplate cloudVisionTemplate;

    /**
     *Constructor
     */
    public ImageService(){ super(); }

    /**
     * Capture the image from given url and stored as an inputStream
     * @return an inputStream of an image from a given url
     * @throws IOException if the url contents cannot be retrieved
     */
    public InputStream get(String url) throws IOException{ return HttpHelper.getAsStream(url);}

    /**
     * call Amazon Web Service (AWS) image Labeling API
     * @param url the url to the image that's being processed by Google Cloud Vision.
     * @return a JSONObject containing results from AWS in the required format
     *         eg, {"id","AL", "labels":[{"label":"Photograph", "relevance": 0.9539},...]}
     */
    public JSONObject AWSImageLabeling(String url){
        ByteBuffer imageBytes;
        BasicAWSCredentials credentials = new BasicAWSCredentials(config.getAWSAccessKey(), config.getAWSSecretKey());
        AmazonRekognition client = AmazonRekognitionClientBuilder.standard()
                .withRegion(Regions.AP_SOUTHEAST_2)
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .build();
        try{
            InputStream in = get(url);
            imageBytes = ByteBuffer.wrap(com.amazonaws.util.IOUtils.toByteArray(in));
            DetectLabelsRequest request = new DetectLabelsRequest()
                    .withImage(new Image().withBytes(imageBytes)).withMaxLabels(10).withMinConfidence(77F);
            try {
                DetectLabelsResult result = client.detectLabels(request);
                List<Label> labels = result.getLabels();
                List<JSONObject> labelsAsJsonObject = new ArrayList<>();

                for (Label l: labels){
                    labelsAsJsonObject.add(new JSONObject().put("label",l.getName()).put("relevance",l.getConfidence()));
                }
                return new JSONObject().put("id","AL").put("labels",new JSONArray(labelsAsJsonObject));
            } catch (AmazonRekognitionException e) {
                log.error("AmazonRekognitionException: "+e.getMessage(),e);
            }
        }catch (IOException e){
            log.error("IOException: "+e.getMessage(),e);
        }
        return new JSONObject().put("id","AL").put("labels","No labels detected");
    }

    /**
     * Call google cloud vision image Labeling API
     * @param url the url to the image that's being processed by Google Cloud Vision.
     * @return a JSONObject containing results from google cloud vision in the required format
     *         eg, {"id","GL", "labels":[{"label":"Photograph", "relevance": 0.9539},...]}
     */
    public JSONObject googleImageLabeling(String url){
        Resource imageResource = resourceLoader.getResource(url);
        AnnotateImageResponse response = cloudVisionTemplate.analyzeImage(imageResource, Feature.Type.LABEL_DETECTION);
        Map<String, Float> imageLabels = response.getLabelAnnotationsList().stream().collect(Collectors.toMap(
                EntityAnnotation::getDescription,
                EntityAnnotation::getScore,
                (u , v)->{
                    throw new IllegalStateException("Duplicate key %s, u");
                },
                LinkedHashMap::new));
        List<JSONObject> labelsAsJsonObject = new ArrayList<>();
        for (String label: imageLabels.keySet()){
            labelsAsJsonObject.add(new JSONObject().put("label",label).put("relevance",imageLabels.get(label)));
        }
        return new JSONObject().put("id","GL").put("labels",new JSONArray(labelsAsJsonObject));
    }

    /**
     * Call microsoft azure computer vision image Labeling API
     * @param url  the url to the image that's being processed by Microsoft azure.
     * @return a JSONObject containing results from microsoft azure  in the required format
     *        eg, {"id","ML", "labels":[{"label":"Photograph", "relevance": 0.9539},...]}
     */
    public JSONObject azureImageLabeling(String url){
        ComputerVisionClient computerVisionClient = ComputerVisionManager
                .authenticate(config.getAzureAccessKey())
                .withEndpoint(config.getAzureEndPoint());
        try {
            InputStream in = get(url);
            byte[] imgBytes = IOUtils.toByteArray(in);
            TagResult labels = computerVisionClient.computerVision().tagImageInStream()
                    .withImage(imgBytes)
                    .withLanguage("en")
                    .execute();
            if (labels.tags().size() != 0) {
                List<JSONObject> labelsAsJsonObject = new ArrayList<>();
                for (ImageTag label : labels.tags()) {
                    labelsAsJsonObject.add(new JSONObject().put("label",label.name()).put("relevance",label.confidence()));
                }
                return new JSONObject().put("id","ML").put("labels",new JSONArray(labelsAsJsonObject));
            }
        } catch (Exception e) {log.error("IOException: "+e.getMessage(),e);}
        return new JSONObject().put("id","ML").put("labels","No labels detected");
    }

    /**
     * Call microsoft azure computer vision image description API
     * @param url  the url to the image that's being processed by Microsoft azure.
     * @return a JSONObject containing results from microsoft azure  in the required format
     *        eg, {"id","MD", "descriptions":[{"description":"Photograph", "relevance": 0.9539},...]}
     */
    public JSONObject azureImageDescription(String url){
        ComputerVisionClient computerVisionClient = ComputerVisionManager
                .authenticate(config.getAzureAccessKey())
                .withEndpoint(config.getAzureEndPoint());
        try {
            InputStream in = get(url);
            byte[] imgBytes = IOUtils.toByteArray(in);
            ImageDescription description = computerVisionClient.computerVision().describeImageInStream()
                    .withImage(imgBytes)
                    .withLanguage("en")
                    .execute();
            if (description.captions().size() != 0) {
                List<JSONObject> labelsAsJsonObject = new ArrayList<>();
                for (ImageCaption caption : description.captions()) {
                    labelsAsJsonObject.add(new JSONObject().put("caption",caption.text()).put("relevance",caption.confidence()));
                }
                return new JSONObject().put("id","MD").put("captionss",new JSONArray(labelsAsJsonObject));
            }
        } catch (Exception e) {log.error("IOException: "+e.getMessage(),e);}
        return new JSONObject().put("id","MD").put("captions","No captions detected");
    }

    /**
     * save the image of given url into local file for visualization
     * @param url the url to the image that's being saved.
     * @param path the location of the saved file
     */
    public void saveImage(String url, String path){
        BufferedImage image = null;
        File outputfile = new File(path);
        try{
            InputStream in = get(url);
            image = ImageIO.read(in);
            ImageIO.write(image,"jpg",outputfile);
        } catch (IOException e){log.error("IOException: "+e.getMessage(),e);}
    }
}
