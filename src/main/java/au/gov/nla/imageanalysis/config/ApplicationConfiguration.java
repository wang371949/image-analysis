package au.gov.nla.imageanalysis.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfiguration {

    @Value("${aws.access-key}")
    private String accessKey;

    @Value("${aws.secret-key}")
    private String secretKey;



    public String getAccessKey(){
        return accessKey;
    }

    public String getSecretKey(){
        return secretKey;
    }

}