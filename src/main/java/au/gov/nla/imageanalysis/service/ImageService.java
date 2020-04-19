package au.gov.nla.imageanalysis.service;


import au.gov.nla.imageanalysis.config.ApplicationConfiguration;
import au.gov.nla.imageanalysis.util.HttpHelper;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.rekognition.model.*;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.EntityAnnotation;
import com.google.cloud.vision.v1.Feature;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.cloud.gcp.vision.CloudVisionTemplate;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.List;


public class ImageService {
    private final String url;

    /**
     *Constructor
     */
    public ImageService(String url){this.url = url;}

    /**
     * Capture the image from given url and stored as an inputStream
     * @return an inputStream of an image from a given url
     * @throws IOException
     */
    public InputStream get() throws IOException{ return HttpHelper.getAsStream(url);}

    /**
     * call Amazon Web Service (AWS) image Labeling API
     *
     * @param config configuration contains AWS access information
     * @return a JSONObject containing results from AWS in the required format
     *         eg, {"id","2", "labels":[{"label":"Photograph", "relevance": 0.9539},...]}
     */
    public JSONObject AWSImageLabeling(ApplicationConfiguration config){
        JSONObject resultLabelsFromAWS =new JSONObject();
        ByteBuffer imageBytes;
        BasicAWSCredentials credentials = new BasicAWSCredentials(config.getAccessKey(), config.getSecretKey());
        AmazonRekognition client = AmazonRekognitionClientBuilder.standard()
                .withRegion(Regions.AP_SOUTHEAST_2)
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .build();
        try{
            InputStream in = get();
            imageBytes = ByteBuffer.wrap(com.amazonaws.util.IOUtils.toByteArray(in));
            DetectLabelsRequest request = new DetectLabelsRequest()
                    .withImage(new Image().withBytes(imageBytes)).withMaxLabels(10).withMinConfidence(77F);
            try {
                DetectLabelsResult result = client.detectLabels(request);
                List<Label> labels = result.getLabels();
                ArrayList<JSONObject> labelsAsJsonObject = new ArrayList<>();

                for (Label l: labels){
                    String label = l.getName();
                    float relevance = l.getConfidence();
                    labelsAsJsonObject.add(new JSONObject().put("label",label).put("relevance",relevance));
                }

                resultLabelsFromAWS.put("id","2").put("labels",new JSONArray(labelsAsJsonObject));

            } catch (AmazonRekognitionException e) {
                e.printStackTrace();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return resultLabelsFromAWS;
    }

    /**
     * call google cloud vision image Labeling API
     *
     * @param resourceLoader  Strategy interface for loading resources. Its getResource method uses an image url to return
     *                        a Resource instance containing the image ByteString.
     * @param cloudVisionTemplate a wrapper around the Vision API Client Libraries and lets you process images easily
     *                            through the Vision API. For more information about the CloudVisionTemplate features, see:
     *                             https://cloud.spring.io/spring-cloud-static/spring-cloud-gcp/1.2.0.RELEASE/reference/html/#google-cloud-vision
     * @return a JSONObject containing results from google cloud vision in the required format
     *         eg, {"id","1", "labels":[{"label":"Photograph", "relevance": 0.9539},...]}
     */
    public JSONObject googleImageLabeling(ResourceLoader resourceLoader, CloudVisionTemplate cloudVisionTemplate){
        JSONObject resultLabelsFromGoogle = new JSONObject();
        Resource imageResource = resourceLoader.getResource(url);
        AnnotateImageResponse response = cloudVisionTemplate.analyzeImage(imageResource, Feature.Type.LABEL_DETECTION);
        Map<String, Float> imageLabels = response.getLabelAnnotationsList().stream().collect(Collectors.toMap(
                EntityAnnotation::getDescription,
                EntityAnnotation::getScore,
                (u , v)->{
                    throw new IllegalStateException("Duplicate key %s, u");
                },
                LinkedHashMap::new));

        ArrayList<JSONObject> labelsAsJsonObject = new ArrayList<>();

        for (String label: imageLabels.keySet()){
            float relevance = imageLabels.get(label);
            labelsAsJsonObject.add(new JSONObject().put("label",label).put("relevance",relevance));
        }

        resultLabelsFromGoogle.put("id","2").put("labels",new JSONArray(labelsAsJsonObject));

        return resultLabelsFromGoogle;
    }
}
