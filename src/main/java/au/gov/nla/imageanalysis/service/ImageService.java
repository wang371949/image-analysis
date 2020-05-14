package au.gov.nla.imageanalysis.service;


import au.gov.nla.imageanalysis.config.ApplicationConfiguration;
import au.gov.nla.imageanalysis.enums.ServiceType;
import au.gov.nla.imageanalysis.util.HttpHelper;
import au.gov.nla.imageanalysis.util.ImageLabel;
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

    /** Constructor */
    public ImageService(){ super(); }

    /*
     * The access to the library computer is not available, so it uses urlReplacement, a online url for trove image access,
     * to create imageService. This is only for testing the functionality of the cloud APIs. Once in the office environment,
     * urlCorrect, should be used to create imageService.
     */
    private String getUrl(String pid){
        String urlCorrect = "https://dl-devel.nla.gov.au/dl-repo/ImageController/"+pid;
        String urlReplacement ="https://trove.nla.gov.au/proxy?url=http://nla.gov.au/nla.obj-142006121-t&md5=IPTuIUjvIhDM3l-IPxq7SQ&expires=1590415200";
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
        List<ImageLabel> imageLabels = new ArrayList<>();
        ByteBuffer imageBytes;
        BasicAWSCredentials credentials = new BasicAWSCredentials(config.getAWSAccessKey(), config.getAWSSecretKey());
        AmazonRekognition client = AmazonRekognitionClientBuilder.standard()
                .withRegion(Regions.AP_SOUTHEAST_2)
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .build();
        try{
            InputStream in = getInputStreamFromUrl(getUrl(pid));
            imageBytes = ByteBuffer.wrap(com.amazonaws.util.IOUtils.toByteArray(in));
            DetectLabelsRequest request = new DetectLabelsRequest()
                    .withImage(new Image().withBytes(imageBytes)).withMaxLabels(10).withMinConfidence(77F);
            try {
                DetectLabelsResult result = client.detectLabels(request);
                for (Label label: result.getLabels()){
                    imageLabels.add(new ImageLabel(label.getName(),label.getConfidence()/100.0f));
                }
                return ImageLabel.covertToJSON(imageLabels, ServiceType.AWS_LABELING_SERVICE, pid);
            } catch (AmazonRekognitionException e) {
                log.error("AWSRekognitionException: "+e.getMessage(),e);
            }
        }catch (IOException e){
            log.error("IOException: "+e.getMessage(),e);
        }
        return ImageLabel.covertToJSON(imageLabels, ServiceType.AWS_LABELING_SERVICE, pid);
    }

    /**
     * Call google cloud vision image Labeling API
     * @param pid the id of the image that's being processed by Google Cloud Vision.
     * @return a JSONObject containing results from google cloud vision in the required format
     *         eg, {"id","GL", "labels":[{"label":"Photograph", "relevance": 0.9539},...]}
     */
    public JSONObject googleImageLabeling(String pid){
        List<ImageLabel> imageLabels = new ArrayList<>();
        Resource imageResource = resourceLoader.getResource(getUrl(pid));
        AnnotateImageResponse response = cloudVisionTemplate.analyzeImage(imageResource, Feature.Type.LABEL_DETECTION);
        Map<String, Float> results = response.getLabelAnnotationsList().stream().collect(Collectors.toMap(
                EntityAnnotation::getDescription,
                EntityAnnotation::getScore,
                (u , v)->{
                    throw new IllegalStateException("Duplicate key %s, u");
                },
                LinkedHashMap::new));
        for (String label: results.keySet()){
            imageLabels.add(new ImageLabel(label,results.get(label)));
        }
        return ImageLabel.covertToJSON(imageLabels, ServiceType.GOOGLE_LABELING_SERVICE, pid);
    }

    /**
     * Call microsoft azure computer vision image Labeling API
     * @param pid the id of the image that's being processed by Microsoft azure.
     * @return a JSONObject containing results from microsoft azure  in the required format
     *        eg, {"id","ML", "labels":[{"label":"Photograph", "relevance": 0.9539},...]}
     */
    public JSONObject azureImageLabeling(String pid){
        List<ImageLabel> imageLabels = new ArrayList<>();
        ComputerVisionClient computerVisionClient = ComputerVisionManager
                .authenticate(config.getAzureAccessKey())
                .withEndpoint(config.getAzureEndPoint());
        try {
            InputStream in = getInputStreamFromUrl(getUrl(pid));
            byte[] imgBytes = IOUtils.toByteArray(in);
            TagResult results = computerVisionClient.computerVision().tagImageInStream()
                    .withImage(imgBytes)
                    .withLanguage("en")
                    .execute();
            if (results.tags().size() != 0) {
                for (ImageTag label : results.tags()) {
                    imageLabels.add(new ImageLabel(label.name(),(float)label.confidence()));
                }
                return ImageLabel.covertToJSON(imageLabels, ServiceType.MICROSOFT_AZURE_LABELING_SERVICE, pid);
            }
        } catch (Exception e) {log.error("IOException: "+e.getMessage(),e);}
        return ImageLabel.covertToJSON(imageLabels, ServiceType.MICROSOFT_AZURE_LABELING_SERVICE, pid);
    }

    /**
     * Call microsoft azure computer vision image description API
     * @param pid the id of the image that's being processed by Microsoft azure.
     * @return a JSONObject containing results from microsoft azure  in the required format
     *        eg, {"id","MD", "descriptions":[{"description":"Photograph", "relevance": 0.9539},...]}
     */
    public JSONObject azureImageDescription(String pid){
        List<ImageLabel> imageLabels = new ArrayList<>();
        ComputerVisionClient computerVisionClient = ComputerVisionManager
                .authenticate(config.getAzureAccessKey())
                .withEndpoint(config.getAzureEndPoint());
        try {
            InputStream in = getInputStreamFromUrl(getUrl(pid));
            byte[] imgBytes = IOUtils.toByteArray(in);
            ImageDescription description = computerVisionClient.computerVision().describeImageInStream()
                    .withImage(imgBytes)
                    .withLanguage("en")
                    .execute();
            if (description.captions().size() != 0) {
                for (ImageCaption caption : description.captions()) {
                    imageLabels.add(new ImageLabel(caption.text(),(float)caption.confidence()));
                }
                return ImageLabel.covertToJSON(imageLabels, ServiceType.MICROSOFT_AZURE_DESCRIPTION_SERVICE, pid);
            }
        } catch (Exception e) {log.error("IOException: "+e.getMessage(),e);}
        return ImageLabel.covertToJSON(imageLabels, ServiceType.MICROSOFT_AZURE_DESCRIPTION_SERVICE, pid);
    }

    /**
     * save the image of given url into local file for visualization
     * @param pid the id of the image that's being saved.
     * @param path the location of the saved file
     */
    public void saveImage(String pid, String path){
        BufferedImage image = null;
        File outputfile = new File(path);
        try{
            InputStream in = getInputStreamFromUrl(getUrl(pid));
            image = ImageIO.read(in);
            ImageIO.write(image,"jpg",outputfile);
        } catch (IOException e){log.error("IOException: "+e.getMessage(),e);}
    }
}
