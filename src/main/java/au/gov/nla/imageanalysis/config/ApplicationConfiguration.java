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



    @Value("${correctUrl}")
    private String correctUrl;

    @Value("${replacementUrl}")
    private String replacementUrl;





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



}