package au.gov.nla.imageanalysis.controllers;


import au.gov.nla.imageanalysis.config.ApplicationConfiguration;
import au.gov.nla.imageanalysis.service.ImageService;
import com.fasterxml.jackson.databind.util.JSONPObject;
import com.google.cloud.vision.v1.*;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.json.JSONArray;
import org.json.JSONObject;
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
import java.lang.reflect.Array;
import java.util.*;
import java.util.stream.Collectors;

@Controller

public class ImageController {

    @Autowired
    //ResourceLoader is to load and  store image data in Resource class
    private ResourceLoader resourceLoader;
    //CloudVisionTemplate will read byteString from Resource and sent to google cloud for labeling process
    @Autowired private CloudVisionTemplate cloudVisionTemplate;
    @Autowired private ApplicationConfiguration config;



    // mapping is to give get request, it passes picture id and other parameters in map
    @RequestMapping(value = "/label/{pid:nla\\.obj-.+}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String getImage(@PathVariable("pid") String pid, @RequestParam List<String> service,
                         HttpServletResponse response, HttpServletRequest request) throws IOException {
        Map<String, String> result = new HashMap<String, String>();

        //Dynamic url is generated here and passed onto the DLC for image extraction
        // The access to the library computer is not available, so it passes a online url for now.
        String urlCorrect = "https://dl-devel.nla.gov.au/dl-repo/ImageController/"+pid;
        //String urlReplacement = "https://assets.readitforward.com/wp-content/uploads/2019/02/RIF-Historical-Fiction-1200x900-830x625.jpg";
        String urlReplacement ="https://trove.nla.gov.au/proxy?url=http://nla.gov.au/nla.obj-159043847-t&md5=O6N-K5SwjBH2ApTGObbxvA&expires=1587996000";
        System.out.println("Correct Url:");
        System.out.println(urlCorrect);
        System.out.println("Replacement URL:");
        System.out.println(urlReplacement);
        //ImageService imageService = new ImageService(urlCorrect);
        ImageService imageService = new ImageService(urlReplacement);

        JSONObject jsonResult = new JSONObject();
        ArrayList resultList = new ArrayList();

        try{
            for (String key: service){
                System.out.println("Parameters contains:");
                System.out.println(key);
                if (key.equals("1") ) {
                    System.out.println("Service code is 1, this is google service");
                    resultList.add(googleImageLabeling(imageService));
                }else if(key.equals("2") ){
                    System.out.println("Service code is 2, this is amonzon service");
                    resultList.add(imageService.amazonDetectLabels(config));
                }else if (key.equals("3") ){
                    System.out.println("Service code is 3, this is to show the image is download from DLC");
                    imageService.callImageService(response);
                }else if (key.equals("4") ){
                    System.out.println("Service code is 4, this is to save the image in DLC in local file");
                    imageService.displayImage();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        JSONObject jsonObject2 = new JSONObject();
        jsonObject2.put("service",resultList);
        JSONArray jsonArray = jsonObject2.getJSONArray("service");
        jsonResult.put("pid",pid);
        jsonResult.put("service",jsonArray);

        System.out.println(jsonResult.toString());

        return jsonResult.toString();
    }

    //google cloud vision method to detect label in the image
    public JSONObject googleImageLabeling(ImageService imageService){
        JSONObject jsonObject = new JSONObject();
        Resource imageResource = this.resourceLoader.getResource(imageService.getUrl());
        AnnotateImageResponse response = this.cloudVisionTemplate.analyzeImage(imageResource, Feature.Type.LABEL_DETECTION);
        Map<String, Float> imageLabels = response.getLabelAnnotationsList().stream().collect(Collectors.toMap(
                EntityAnnotation::getDescription,
                EntityAnnotation::getScore,
                (u , v)->{
                    throw new IllegalStateException(String.format("Duplicate key %s, u"));
                },
                LinkedHashMap::new));
        // so far, I prints out the label and relavence on the console, need to update to return a json file
        //System.out.println("Results from google cloud vision:");
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
