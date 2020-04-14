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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gcp.vision.CloudVisionTemplate;
import org.springframework.core.io.ResourceLoader;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;


import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.Feature.Type;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.protobuf.ByteString;
import com.google.protobuf.util.JsonFormat;
import java.io.FileInputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class ImageService {
    private String url;
//    @Autowired
//    private ResourceLoader resourceLoader;
//    @Autowired private CloudVisionTemplate cloudVisionTemplate;

    public ImageService(String url){
        this.url = url;
    }

    //method to capture the image from DLC as inputStrean
    public InputStream get() throws IOException{
        return HttpHelper.getAsStream(url);
    }

    //method to show image on browers, just to confirm the image is downloaded from DLC
    public void callImageService(HttpServletResponse response) throws Exception{
        try(InputStream in = get()){
            try(OutputStream out = response.getOutputStream()){
                IOUtils.copy(in,out);
            }
        }
    }

    //method to write downloaded image to local file
    public void displayImage(){
        BufferedImage image = null;
        File outputfile = new File("src/main/java/image.jpg");
        try{
            InputStream in = get();
            image = ImageIO.read(in);
            ImageIO.write(image,"jpg",outputfile);
        }catch (IOException e){
            e.printStackTrace();
        }
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
                //System.out.println(jsonObject.toString());

//                System.out.println("Detected labels for " + getUrl());
//                for (Label label: labels) {
//                    System.out.println(label.getName() + ": " + label.getConfidence().toString());
//                }

            } catch (AmazonRekognitionException e) {
                e.printStackTrace();
            }

        }catch (Exception e){
            e.printStackTrace();
        }

        return jsonObject;

    }

    //google's method to send image for labeling. only for reference because an easier way to do this is discovered
//    public void detectLabels( PrintStream out) throws Exception, IOException {
//        List<AnnotateImageRequest> requests = new ArrayList<>();
//
//        InputStream in = get();
//
//        ByteString imgBytes = ByteString.readFrom(in);
//
//        Image img = Image.newBuilder().setContent(imgBytes).build();
//        Feature feat = Feature.newBuilder().setType(Type.LABEL_DETECTION).build();
//        AnnotateImageRequest request =
//                AnnotateImageRequest.newBuilder().addFeatures(feat).setImage(img).build();
//        requests.add(request);
//
//        try (ImageAnnotatorClient client = ImageAnnotatorClient.create()) {
//            BatchAnnotateImagesResponse response = client.batchAnnotateImages(requests);
//            List<AnnotateImageResponse> responses = response.getResponsesList();
//
//            for (AnnotateImageResponse res : responses) {
//                if (res.hasError()) {
//                    out.printf("Error: %s\n", res.getError().getMessage());
//                    return;
//                }
//
//                // For full list of available annotations, see http://g.co/cloud/vision/docs
//                for (EntityAnnotation annotation : res.getLabelAnnotationsList()) {
//                    annotation.getAllFields().forEach((k, v) -> out.printf("%s : %s\n", k, v.toString()));
//                }
//            }
//        }
//    }



     //this method is current in image controller. It should be here. experiencing problems with nullpointerexception. need to be fixed
//    public void googleImageLabeling(){
//        System.out.println("point1");
//        AnnotateImageResponse response = this.cloudVisionTemplate.analyzeImage(this.resourceLoader.getResource(this.url), Feature.Type.LABEL_DETECTION);
//        System.out.println("point2");
//        Map<String, Float> imageLabels = response.getLabelAnnotationsList().stream().collect(Collectors.toMap(
//                EntityAnnotation::getDescription,
//                EntityAnnotation::getScore,
//                (u , v)->{
//                    throw new IllegalStateException(String.format("Duplicate key %s, u"));
//                },
//                LinkedHashMap::new));
//        System.out.println("point3");
//
//        for (String key: imageLabels.keySet()){
//            System.out.println(key);
//            System.out.println(imageLabels.get(key));
//        }
//
//
//    }







    public String getUrl() {
        return url;
    }

    /**
     * Determines the query using the requestParams and this.url and this.workPid
     */


}
