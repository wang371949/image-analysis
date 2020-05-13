package au.gov.nla.imageanalysis.controllers;


import au.gov.nla.imageanalysis.service.ImageService;
import au.gov.nla.imageanalysis.enums.ServiceType;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.util.*;


@Controller
public class ImageController {

    private final Logger log = LoggerFactory.getLogger(ImageController.class);

    @Autowired
    ImageService imageService;

    /**
     * The method will obtain image by from url: "https://dl-devel.nla.gov.au/dl-repo/ImageController/"+pid. It then calls
     * the selected cloud image labeling services.
     *
     * @param pid  The picture ID. For example: nla.obj-159043847
     * @param service  a list of String number, each number points to a cloud service. eg, 1 = google labeling service, 2 = AWS labeling service
     * @return finalResultAsJsonObject.toString(): printed version of the JSON object that contains the results from the selected cloud
     *         labeling services. Format {"pid":"nla.obj-159043847", "service":[JSON object from calling googleImageLabeling,
     *         JSON object from calling AWSImageLabeling,...]}. Please refer to project plan for details.
     */
    @RequestMapping(value = "/label/{pid:nla\\.obj-.+}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String getImage(@PathVariable("pid") String pid, @RequestParam List<String> service) {

        /*
         * The access to the library computer is not available, so it uses urlReplacement, a online url for trove image access,
         * to create imageService. This is only for testing the functionality of the cloud APIs. Once in the office environment,
         * urlCorrect, should be used to create imageService.
         */
        String urlCorrect = "https://dl-devel.nla.gov.au/dl-repo/ImageController/"+pid;
        String urlReplacement ="https://trove.nla.gov.au/proxy?url=http://nla.gov.au/nla.obj-159043847-t&md5=O6N-K5SwjBH2ApTGObbxvA&expires=1587996000";
        log.info("Correct Url: {}",urlCorrect);
        log.info("Replacement URL: {}", urlReplacement);
        //ImageService imageService = new ImageService(urlCorrect);

        return new JSONObject().put("pid",pid)
                .put("service",new JSONArray(callImageServices(service, urlReplacement)))
                .toString();
    }

    /**
     * This method processes an image with selected services and stores the returned JSON objects into a JSON array
     *
     * @param service  a list of String number, each number points to a cloud service. eg, 1 = google labeling service, 2 = AWS labeling service
     * @param url  the url to the processing image
     * @return a JSON array containing results from selected cloud labeling services in the required format.
     * eg, [JSON object from calling googleImageLabeling, JSON object from calling AWSImageLabeling,...]
     */

    private List<JSONObject> callImageServices(List<String> service, String url) {
        List<JSONObject> ArrayOfLabelsFromSelectedServices = new ArrayList<>();
        for (String key : service) {
            if (key.equals(ServiceType.GOOGLE_LABELING_SERVICE.code())) {
                log.info("Parameters contains: {}, the result contains google service", key);
                ArrayOfLabelsFromSelectedServices.add(imageService.googleImageLabeling(url));
            } else if (key.equals(ServiceType.AWS_LABELING_SERVICE.code())) {
                log.info("Parameters contains: {}, the result contains aws service", key);
                ArrayOfLabelsFromSelectedServices.add(imageService.AWSImageLabeling(url));
            }
        }
        return ArrayOfLabelsFromSelectedServices;
    }
}
