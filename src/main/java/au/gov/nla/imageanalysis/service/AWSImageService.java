package au.gov.nla.imageanalysis.service;


import au.gov.nla.imageanalysis.util.HttpHelper;
import au.gov.nla.imageanalysis.util.ImageLabel;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.rekognition.model.*;
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
     * @param url the url of the image that's being processed by Google Cloud Vision.
     * @return a JSONObject containing results from AWS in the required format
     *         eg, {"id","AL", "labels":[{"label":"Photograph", "relevance": 0.9539},...]}
     */
    public List<ImageLabel> AWSImageLabeling(String url, String accessKey, String secretKey){
        List<ImageLabel> serviceOutput = new ArrayList<>();
        ByteBuffer imageBytes;
        BasicAWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
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
                    serviceOutput.add(new ImageLabel(label.getName(),label.getConfidence()/100.0f));
                }
                return serviceOutput;
            } catch (AmazonRekognitionException e) {
                log.error("AWSRekognitionException: "+e.getMessage(),e);
            }
        }catch (IOException e){
            log.error("IOException: "+e.getMessage(),e);
        }
        return serviceOutput;
    }

}
