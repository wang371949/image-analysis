package au.gov.nla.imageanalysis.controllers;


import au.gov.nla.imageanalysis.service.ImageService;
import com.google.cloud.vision.v1.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gcp.vision.CloudVisionTemplate;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;



import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

@Controller

public class ImageController {

    @Autowired

    //ResourceLoader is to load and  store image data in Resource class
    private ResourceLoader resourceLoader;
    //CloudVisionTemplate will read byteString from Resource and sent to google cloud for labeling process
    @Autowired private CloudVisionTemplate cloudVisionTemplate;

    // mapping is to give get request, it passes picture id and other parameters in map
    @RequestMapping(value = "/label/{pid:nla\\.obj-.+}", method = RequestMethod.GET, produces = MediaType.IMAGE_JPEG_VALUE)
    @ResponseBody
    public void getImage(@PathVariable("pid") String pid, @RequestParam Map<String,String> allRequestParams,
                         HttpServletResponse response, HttpServletRequest request) throws IOException {
        Map<String, String> result = new HashMap<String, String>();

        //Dynamic url is generated here and passed onto the DLC for image extraction
        // The access to the library computer is not available, so it passes a online url for now.
        String urlCorrect = "https://dl-devel.nla.gov.au/dl-repo/ImageController/"+pid;
        String urlReplacement = "https://assets.readitforward.com/wp-content/uploads/2019/02/RIF-Historical-Fiction-1200x900-830x625.jpg";
        System.out.println("Correct Url:");
        System.out.println(urlCorrect);
        System.out.println("Replacement URL:");
        System.out.println(urlReplacement);
        //ImageService imageService = new ImageService(urlCorrect);
        ImageService imageService = new ImageService(urlReplacement);
        try{
            if (allRequestParams.containsKey("service")){
                int serviceType = Integer.valueOf(allRequestParams.get("service"));
                if (serviceType ==1 ) {
                    System.out.println("Service code is 1, this is google service");
                    googleImageLabeling(imageService);
                }else if(serviceType ==2){
                    System.out.println("Service code is 2, this is amonzon service");
                    amonzonImageLabeling(imageService);
                }else if (serviceType ==3){
                    System.out.println("Service code is 3, this is to show the image is download from DLC");
                    imageService.callImageService(response);
                }else if (serviceType ==4){
                    System.out.println("Service code is 4, this is to save the image in DLC in local file");
                    imageService.displayImage();
                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }

        for (String key: allRequestParams.keySet()){
            System.out.println("Parameters in the get request:");
            System.out.println(key);
            System.out.println(allRequestParams.get(key));
        }
    }

    //google cloud vision method to detect label in the image
    public void googleImageLabeling(ImageService imageService){
        Resource imageResource = this.resourceLoader.getResource("https://assets.readitforward.com/wp-content/uploads/2019/02/RIF-Historical-Fiction-1200x900-830x625.jpg");
        AnnotateImageResponse response = this.cloudVisionTemplate.analyzeImage(imageResource, Feature.Type.LABEL_DETECTION);
        Map<String, Float> imageLabels = response.getLabelAnnotationsList().stream().collect(Collectors.toMap(
                EntityAnnotation::getDescription,
                EntityAnnotation::getScore,
                (u , v)->{
                    throw new IllegalStateException(String.format("Duplicate key %s, u"));
                },
                LinkedHashMap::new));
        // so far, I prints out the label and relavence on the console, need to update to return a json file
        System.out.println("Results from google cloud vision:");
        for (String key: imageLabels.keySet()){
            System.out.println(key);
            System.out.println(imageLabels.get(key));
        }
    }

    public void amonzonImageLabeling(ImageService imageService){
        //need to be implemented
    }


//    public void detectLabels(ImageService imageService, PrintStream out) throws Exception, IOException {
//        List<AnnotateImageRequest> requests = new ArrayList<>();
//
//        InputStream in = imageService.get();
//
//        ByteString imgBytes = ByteString.readFrom(in);
//
//        Image img = Image.newBuilder().setContent(imgBytes).build();
//        Feature feat = Feature.newBuilder().setType(Feature.Type.LABEL_DETECTION).build();
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



//    public Map<String, String> getImage(@PathVariable("pid") String pid, @RequestParam String service){
////        String urlString = "https://dl-devel.nla.gov.au/dl-repo/ImageController/"+pid;
////        try {
////            URL url = new URL(urlString);
////            try {
////                BufferedImage bufferedImage = ImageIO.read(url);
////                System.out.println(bufferedImage);
////            } catch (IOException e) {
////                e.printStackTrace();
////            }
////        } catch (MalformedURLException e) {
////            e.printStackTrace();
////        }
//        //String pid = null;
//        //Use http client to retrive image data using pid
//        //https://dl-devel.nla.gov.au/dl-repo/ImageController/nla.obj-398310603
//        //process data
//
//        Map<String, String> result = new HashMap<String, String>();
//        result.put("pid", pid);
//        result.put("service", service);
//        return result;
//    }




}
