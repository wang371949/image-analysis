package au.gov.nla.imageanalysis.service;

import au.gov.nla.imageanalysis.config.ApplicationConfiguration;
import au.gov.nla.imageanalysis.enums.ServiceType;
import au.gov.nla.imageanalysis.util.HttpHelper;
import au.gov.nla.imageanalysis.util.ImageLabel;
import com.amazonaws.util.IOUtils;
import com.microsoft.azure.cognitiveservices.vision.computervision.*;
import com.microsoft.azure.cognitiveservices.vision.computervision.models.*;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


@Component
public class AzureImageService {
    private final Logger log = LoggerFactory.getLogger(ImageService.class);


    /**
     * Call microsoft azure computer vision image Labeling API
     * @param pid the id of the image that's being processed by Microsoft azure.
     * @param url the url of the image that's being processed by Microsoft azure.
     * @return a JSONObject containing results from microsoft azure  in the required format
     *        eg, {"id","ML", "labels":[{"label":"Photograph", "relevance": 0.9539},...]}
     */
    public JSONObject azureImageLabeling(String pid, String url, ApplicationConfiguration config){
        List<ImageLabel> imageLabels = new ArrayList<>();
        ComputerVisionClient computerVisionClient = ComputerVisionManager
                .authenticate(config.getAzureAccessKey())
                .withEndpoint(config.getAzureEndPoint());
        try {
            InputStream in = HttpHelper.getAsStream(url);
            byte[] imgBytes = IOUtils.toByteArray(in);
            TagResult results = computerVisionClient.computerVision().tagImageInStream()
                    .withImage(imgBytes)
                    .withLanguage("en")
                    .execute();
            for (ImageTag label : results.tags()) {
                imageLabels.add(new ImageLabel(label.name(), (float) label.confidence()));
            }
            return ImageLabel.covertToJSON(imageLabels, ServiceType.ML, pid);
        } catch (Exception e) {log.error("IOException: "+e.getMessage(),e);}
        return ImageLabel.covertToJSON(imageLabels, ServiceType.ML, pid);
    }

    /**
     * Call microsoft azure computer vision image description API
     * @param pid the id of the image that's being processed by Microsoft azure.
     * @param url the url of the image that's being processed by Microsoft azure.
     * @return a JSONObject containing results from microsoft azure  in the required format
     *        eg, {"id","MD", "descriptions":[{"description":"Photograph", "relevance": 0.9539},...]}
     */
    public JSONObject azureImageDescription(String pid, String url, ApplicationConfiguration config){
        List<ImageLabel> imageLabels = new ArrayList<>();
        ComputerVisionClient computerVisionClient = ComputerVisionManager
                .authenticate(config.getAzureAccessKey())
                .withEndpoint(config.getAzureEndPoint());
        try {
            InputStream in = HttpHelper.getAsStream(url);
            byte[] imgBytes = IOUtils.toByteArray(in);
            ImageDescription description = computerVisionClient.computerVision().describeImageInStream()
                    .withImage(imgBytes)
                    .withLanguage("en")
                    .execute();
            for (ImageCaption caption : description.captions()) {
                imageLabels.add(new ImageLabel(caption.text(), (float) caption.confidence()));
            }
            return ImageLabel.covertToJSON(imageLabels, ServiceType.MD, pid);
        } catch (Exception e) {log.error("IOException: "+e.getMessage(),e);}
        return ImageLabel.covertToJSON(imageLabels, ServiceType.MD, pid);
    }

}
