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
    public ImageService(String url){
        this.url = url;
    }

    //method to capture the image from DLC as inputStrean
    public InputStream get() throws IOException{
        return HttpHelper.getAsStream(url);
    }

    public JSONObject AWSDetectLabels(ApplicationConfiguration config){
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
