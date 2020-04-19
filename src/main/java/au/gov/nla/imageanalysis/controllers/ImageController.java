package au.gov.nla.imageanalysis.controllers;


import au.gov.nla.imageanalysis.config.ApplicationConfiguration;
import au.gov.nla.imageanalysis.service.ImageService;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gcp.vision.CloudVisionTemplate;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;



import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;


@Controller
public class ImageController {

    private Logger log = LoggerFactory.getLogger(ImageController.class);

    //ResourceLoader is to load and  store image data in Resource class
    @Autowired private ResourceLoader resourceLoader;
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
        log.info("Correct Url: {}",urlCorrect);
        log.info("Replacement URL: {}", urlReplacement);
        //ImageService imageService = new ImageService(urlCorrect);
        ImageService imageService = new ImageService(urlReplacement);

        JSONObject jsonResult = new JSONObject();
        ArrayList resultList = new ArrayList();

        try{
            for (String key: service){
                if (key.equals("1") ) {
                    log.info("Parameters contains: {}, the result contains google service",key);
                    resultList.add(imageService.googleImageLabeling(resourceLoader,cloudVisionTemplate));
                }else if(key.equals("2") ){
                    log.info("Parameters contains: {}, the result contains aws service",key);
                    resultList.add(imageService.amazonDetectLabels(config));
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
        log.info(jsonResult.toString());
        return jsonResult.toString();
    }
}
