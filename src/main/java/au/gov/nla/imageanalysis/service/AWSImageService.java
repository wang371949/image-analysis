package au.gov.nla.imageanalysis.service;


import au.gov.nla.imageanalysis.enums.ServiceType;
import au.gov.nla.imageanalysis.logic.ImageLabel;
import au.gov.nla.imageanalysis.logic.ImageLabels;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.rekognition.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.nio.ByteBuffer;



@Component
public class AWSImageService {

    private final Logger log = LoggerFactory.getLogger(ImageService.class);

    /**
     * call Amazon Web Service (AWS) image Labeling API
     * @param imageAsByteArray the image that's being processed by AWS.
     * @return a JSONObject containing results from AWS in the required format
     *         eg, {"id","AL", "labels":[{"label":"Photograph", "relevance": 0.9539},...]}
     */
    public ImageLabels AWSImageLabeling(byte [] imageAsByteArray, String accessKey, String secretKey){
        ImageLabels outputLabels = new ImageLabels(ServiceType.AL);
        ByteBuffer imageBytes;
        BasicAWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
        AmazonRekognition client = AmazonRekognitionClientBuilder.standard()
                .withRegion(Regions.AP_SOUTHEAST_2)
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .build();
        imageBytes = ByteBuffer.wrap(imageAsByteArray);
        DetectLabelsRequest request = new DetectLabelsRequest()
                .withImage(new Image().withBytes(imageBytes)).withMaxLabels(10).withMinConfidence(77F);
        try {
            DetectLabelsResult result = client.detectLabels(request);
            for (Label label: result.getLabels()){
                outputLabels.addImageLabel(new ImageLabel(label.getName(),label.getConfidence()/100.0f));
            }
            return outputLabels;
        } catch (AmazonRekognitionException e) {
            log.error("AWSRekognitionException: "+e.getMessage(),e);
        }
        return outputLabels;
    }
}
