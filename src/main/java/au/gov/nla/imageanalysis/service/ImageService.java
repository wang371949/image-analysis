package au.gov.nla.imageanalysis.service;


import au.gov.nla.imageanalysis.util.HttpHelper;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.EntityAnnotation;
import com.google.cloud.vision.v1.Feature;
import org.apache.tomcat.util.http.fileupload.IOUtils;
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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;


import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.Feature.Type;
import com.google.cloud.vision.v1.Image;
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
