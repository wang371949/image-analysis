package au.gov.nla.imageanalysis.service;

import au.gov.nla.imageanalysis.config.ApplicationConfiguration;
import au.gov.nla.imageanalysis.enums.ServiceType;
import au.gov.nla.imageanalysis.util.HttpHelper;
import au.gov.nla.imageanalysis.util.ImageLabel;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.rekognition.model.*;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;


@Component
public class AWSImageService {

    private final Logger log = LoggerFactory.getLogger(ImageService.class);

    /**
     * call Amazon Web Service (AWS) image Labeling API
     * @param pid the id of the image that's being processed by Google Cloud Vision.
     * @param url the url of the image that's being processed by Google Cloud Vision.
     * @return a JSONObject containing results from AWS in the required format
     *         eg, {"id","AL", "labels":[{"label":"Photograph", "relevance": 0.9539},...]}
     */
    public JSONObject AWSImageLabeling(String pid, String url, ApplicationConfiguration config){
        List<ImageLabel> imageLabels = new ArrayList<>();
        ByteBuffer imageBytes;
        BasicAWSCredentials credentials = new BasicAWSCredentials(config.getAWSAccessKey(), config.getAWSSecretKey());
        AmazonRekognition client = AmazonRekognitionClientBuilder.standard()
                .withRegion(Regions.AP_SOUTHEAST_2)
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .build();
        try{
            InputStream in = HttpHelper.getAsStream(url);
            imageBytes = ByteBuffer.wrap(com.amazonaws.util.IOUtils.toByteArray(in));
            DetectLabelsRequest request = new DetectLabelsRequest()
                    .withImage(new Image().withBytes(imageBytes)).withMaxLabels(10).withMinConfidence(77F);
            try {
                DetectLabelsResult result = client.detectLabels(request);
                for (Label label: result.getLabels()){
                    imageLabels.add(new ImageLabel(label.getName(),label.getConfidence()/100.0f));
                }
                return ImageLabel.covertToJSON(imageLabels, ServiceType.AL, pid);
            } catch (AmazonRekognitionException e) {
                log.error("AWSRekognitionException: "+e.getMessage(),e);
            }
        }catch (IOException e){
            log.error("IOException: "+e.getMessage(),e);
        }
        return ImageLabel.covertToJSON(imageLabels, ServiceType.AL, pid);
    }

}
