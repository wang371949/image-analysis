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
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.cloud.gcp.vision.CloudVisionTemplate;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.List;


public class ImageService {
    private String url;
    public ImageService(String url){
        this.url = url;
    }

    //method to capture the image from DLC as inputStrean
    public InputStream get() throws IOException{
        return HttpHelper.getAsStream(url);
    }

    public JSONObject amazonDetectLabels(ApplicationConfiguration config) throws IOException {
        JSONObject jsonObject =new JSONObject();
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

                //System.out.println("Results from amazon Rekognition:");
                ArrayList list = new ArrayList();
                for (Label l: labels){
                    String label = l.getName();
                    float relevance = l.getConfidence();
                    JSONObject jsonObject1 = new JSONObject();
                    jsonObject1.put("label",label);
                    jsonObject1.put("relevance",relevance);
                    list.add(jsonObject1);
                }
                JSONObject jsonObject2 = new JSONObject();
                jsonObject2.put("label",list);
                JSONArray jsonArray = jsonObject2.getJSONArray("label");
                jsonObject.put("id","2");
                jsonObject.put("labels",jsonArray);
            } catch (AmazonRekognitionException e) {
                e.printStackTrace();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return jsonObject;
    }

    public JSONObject googleImageLabeling(ResourceLoader resourceLoader, CloudVisionTemplate cloudVisionTemplate){
        JSONObject jsonObject = new JSONObject();
        Resource imageResource = resourceLoader.getResource(url);
        AnnotateImageResponse response = cloudVisionTemplate.analyzeImage(imageResource, Feature.Type.LABEL_DETECTION);
        Map<String, Float> imageLabels = response.getLabelAnnotationsList().stream().collect(Collectors.toMap(
                EntityAnnotation::getDescription,
                EntityAnnotation::getScore,
                (u , v)->{
                    throw new IllegalStateException(String.format("Duplicate key %s, u"));
                },
                LinkedHashMap::new));

        ArrayList list = new ArrayList();
        for (String label: imageLabels.keySet()){
            float relevance = imageLabels.get(label);
            JSONObject jsonObject1 = new JSONObject();
            jsonObject1.put("label",label);
            jsonObject1.put("relevance",relevance);
            list.add(jsonObject1);
        }
        JSONObject jsonObject2 = new JSONObject();
        jsonObject2.put("label",list);
        JSONArray jsonArray = jsonObject2.getJSONArray("label");
        jsonObject.put("id","1");
        jsonObject.put("labels",jsonArray);
        //System.out.println(jsonObject.toString());

        return jsonObject;
    }

    public String getUrl() {
        return url;
    }

    /**
     * Determines the query using the requestParams and this.url and this.workPid
     */


}
