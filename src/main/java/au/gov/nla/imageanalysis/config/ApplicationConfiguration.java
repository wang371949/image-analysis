package au.gov.nla.imageanalysis.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfiguration {
    /**   AWS access keys    */
    @Value("${aws.access-key}")
    private String awsAccessKey;
    @Value("${aws.secret-key}")
    private String awsSecretKey;

    /**   Microsoft azure access keys*/
    @Value("${azure.access-key}")
    private String azureAccessKey;
    @Value("${azure.endpoint}")
    private String azureEndPoint;

    @Value("5")
    private int maxNumLabelsPerService;

    @Value("${correctUrl}")
    private String correctUrl;

    @Value("${replacementUrl}")
    private String replacementUrl;

    @Value("${imageSaveLocation}")
    private String imageSaveLocation;

    @Value("${testLabelLocation}")
    private String testLabelLocation;

    @Value("${testImageLocation}")
    private String testImageLocation;

    private float wordThreshold = 0.9f;






    public String getAWSAccessKey(){
        return awsAccessKey;
    }
    public String getAWSSecretKey(){
        return awsSecretKey;
    }

    public String getAzureAccessKey(){
        return azureAccessKey;
    }
    public String getAzureEndPoint(){
        return azureEndPoint;
    }

    public String getCorrectUrl(){
        return correctUrl;
    }

    public String getReplacementUrl(){
        return replacementUrl;
    }

    public String getImageSaveLocation(){ return imageSaveLocation;}

    public String getTestLabelLocation(){return testLabelLocation;}

    public String getTestImageLocation(){return testImageLocation;}

    public int getMaxNumLabelsPerService(){return maxNumLabelsPerService;}

    public float getWordThreshold(){return wordThreshold;}



}