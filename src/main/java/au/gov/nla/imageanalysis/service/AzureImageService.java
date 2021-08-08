package au.gov.nla.imageanalysis.service;


import au.gov.nla.imageanalysis.enums.ServiceType;
import au.gov.nla.imageanalysis.logic.ImageLabel;
import au.gov.nla.imageanalysis.logic.ImageLabels;
import com.microsoft.azure.cognitiveservices.vision.computervision.*;
import com.microsoft.azure.cognitiveservices.vision.computervision.models.*;
import org.springframework.stereotype.Component;



@Component
public class AzureImageService {

    /**
     * Call microsoft azure computer vision image Labeling API
     * @param imageAsByteArray the image that's being processed by microsoft azure.
     * @return a JSONObject containing results from microsoft azure  in the required format
     *        eg, {"id","ML", "labels":[{"label":"Photograph", "relevance": 0.9539},...]}
     */
    public ImageLabels azureImageLabeling(byte [] imageAsByteArray, String accessKey, String endPoint){
        ImageLabels outputLabels = new ImageLabels(ServiceType.ML);
        ComputerVisionClient computerVisionClient = ComputerVisionManager
                .authenticate(accessKey)
                .withEndpoint(endPoint);
        TagResult results = computerVisionClient.computerVision().tagImageInStream()
                .withImage(imageAsByteArray)
                .withLanguage("en")
                .execute();
        for (ImageTag label : results.tags()) {
            outputLabels.addImageLabel(new ImageLabel(label.name(), (float) label.confidence()));
        }
        return outputLabels;
    }

    /**
     * Call microsoft azure computer vision image description API
     * @param imageAsByteArray the image that's being processed by microsoft azure.
     * @return a JSONObject containing results from microsoft azure  in the required format
     *        eg, {"id","MD", "descriptions":[{"description":"Photograph", "relevance": 0.9539},...]}
     */
    public ImageLabels azureImageDescription(byte [] imageAsByteArray, String accessKey, String endPoint){
        ImageLabels serviceOutput = new ImageLabels(ServiceType.MD);
        ComputerVisionClient computerVisionClient = ComputerVisionManager
                .authenticate(accessKey)
                .withEndpoint(endPoint);
        ImageDescription description = computerVisionClient.computerVision().describeImageInStream()
                .withImage(imageAsByteArray)
                .withLanguage("en")
                .execute();
        for (ImageCaption caption : description.captions()) {
            serviceOutput.addImageLabel(new ImageLabel(caption.text(), (float) caption.confidence()));
        }
        return serviceOutput;
    }
}
