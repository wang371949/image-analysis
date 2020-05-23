package au.gov.nla.imageanalysis.service;



import au.gov.nla.imageanalysis.enums.ServiceType;
import au.gov.nla.imageanalysis.logic.ImageLabel;
import au.gov.nla.imageanalysis.logic.ImageLabels;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.EntityAnnotation;
import com.google.cloud.vision.v1.Feature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gcp.vision.CloudVisionTemplate;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;


import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;



@Component
public class GoogleImageService {

    /**
     * The CloudVisionTemplate is a wrapper around the Vision API Client Libraries and lets you process images easily
     * through the Vision API. For more information about the CloudVisionTemplate features, see:
     * https://cloud.spring.io/spring-cloud-static/spring-cloud-gcp/1.2.0.RELEASE/reference/html/#google-cloud-vision
     */
    @Autowired
    private CloudVisionTemplate cloudVisionTemplate;

    /**
     * Call google cloud vision image Labeling API
     * @param imageAsByteArray the image that's being processed by Google Cloud Vision.
     * @return a JSONObject containing results from google cloud vision in the required format
     *         eg, {"id","GL", "labels":[{"label":"Photograph", "relevance": 0.9539},...]}
     */
    public ImageLabels googleImageLabeling(byte[] imageAsByteArray){
        ImageLabels outputLabels = new ImageLabels(ServiceType.GL);
        Resource imageResource = new ByteArrayResource(imageAsByteArray);
        AnnotateImageResponse response = cloudVisionTemplate.analyzeImage(imageResource, Feature.Type.LABEL_DETECTION);
        Map<String, Float> results = response.getLabelAnnotationsList().stream().collect(Collectors.toMap(
                EntityAnnotation::getDescription,
                EntityAnnotation::getScore,
                (u , v)->{
                    throw new IllegalStateException("Duplicate key %s, u");
                },
                LinkedHashMap::new));
        for (String label: results.keySet()){
            outputLabels.addImageLabel(new ImageLabel(label,results.get(label)));
        }
        return outputLabels;
    }

}
