package au.gov.nla.imageanalysis.controllers;


import au.gov.nla.imageanalysis.service.ImageService;
import au.gov.nla.imageanalysis.enums.ServiceType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.*;


@Controller
public class ImageController {

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
    public String getImage(@PathVariable("pid") String pid, @RequestParam List<ServiceType> service) {
        return imageService.callImageServices(pid,service).toJSON().toString();
    }

    /**
     * This method allows the processes of image from a local folder and stores all the results in a csv file
     * @param service a list of String number, each number points to a cloud service. eg, 1 = google labeling service, 2 = AWS labeling service
     */
    @RequestMapping(value = "/labels", method = RequestMethod.GET)
    @ResponseBody
    public void processImages(@RequestParam List<ServiceType> service, HttpServletResponse response) {
        imageService.processImages(service, response);
    }
}
