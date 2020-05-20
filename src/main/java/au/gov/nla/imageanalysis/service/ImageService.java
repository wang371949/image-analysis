package au.gov.nla.imageanalysis.service;


import au.gov.nla.imageanalysis.config.ApplicationConfiguration;
import au.gov.nla.imageanalysis.util.HttpHelper;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

@Component
public class ImageService {
    private final Logger log = LoggerFactory.getLogger(ImageService.class);
    @Autowired private ApplicationConfiguration config;
    @Autowired private AWSImageService awsImageService;
    @Autowired private GoogleImageService googleImageService;
    @Autowired private AzureImageService azureImageService;

    /** Constructor */
    public ImageService(){ super(); }

    /*
     * The access to the library computer is not available, so it uses urlReplacement, a online url for trove image access,
     * to create imageService. This is only for testing the functionality of the cloud APIs. Once in the office environment,
     * urlCorrect, should be used to create imageService.
     */
    private String getUrl(String pid){
        String urlCorrect = config.getCorrectUrl()+pid;
        String urlReplacement = config.getReplacementUrl();
        log.info("Correct Url: {}",urlCorrect);
        log.info("Replacement URL: {}", urlReplacement);
        return urlReplacement;
    }

    /**
     * Capture the image from given url and stored as an inputStream
     * @return an inputStream of an image from a given url
     * @throws IOException if the url contents cannot be retrieved
     */
    public InputStream getInputStreamFromUrl(String url) throws IOException{ return HttpHelper.getAsStream(url);}

    /**
     * call Amazon Web Service (AWS) image Labeling API
     * @param pid the id of the image that's being processed by Google Cloud Vision.
     * @return a JSONObject containing results from AWS in the required format
     *         eg, {"id","AL", "labels":[{"label":"Photograph", "relevance": 0.9539},...]}
     */
    public JSONObject AWSImageLabeling(String pid){return awsImageService.AWSImageLabeling(pid, getUrl(pid), config);}

    /**
     * Call google cloud vision image Labeling API
     * @param pid the id of the image that's being processed by Google Cloud Vision.
     * @return a JSONObject containing results from google cloud vision in the required format
     *         eg, {"id","GL", "labels":[{"label":"Photograph", "relevance": 0.9539},...]}
     */
    public JSONObject googleImageLabeling(String pid){return googleImageService.googleImageLabeling(pid,getUrl(pid));}

    /**
     * Call microsoft azure computer vision image Labeling API
     * @param pid the id of the image that's being processed by Microsoft azure.
     * @return a JSONObject containing results from microsoft azure  in the required format
     *        eg, {"id","ML", "labels":[{"label":"Photograph", "relevance": 0.9539},...]}
     */
    public JSONObject azureImageLabeling(String pid){return azureImageService.azureImageLabeling(pid, getUrl(pid),config);}

    /**
     * Call microsoft azure computer vision image description API
     * @param pid the id of the image that's being processed by Microsoft azure.
     * @return a JSONObject containing results from microsoft azure  in the required format
     *        eg, {"id","MD", "descriptions":[{"description":"Photograph", "relevance": 0.9539},...]}
     */
    public JSONObject azureImageDescription(String pid){return azureImageService.azureImageDescription(pid,getUrl(pid),config);}

    /**
     * save the image of given url into local file for visualization
     * @param pid the id of the image that's being saved.
     * @param path the location of the saved file
     */
    public void saveImage(String pid, String path){
        BufferedImage image = null;
        File outputfile = new File(path);
        try{
            InputStream in = getInputStreamFromUrl(getUrl(pid));
            image = ImageIO.read(in);
            ImageIO.write(image,"jpg",outputfile);
        } catch (IOException e){log.error("IOException: "+e.getMessage(),e);}
    }
}
