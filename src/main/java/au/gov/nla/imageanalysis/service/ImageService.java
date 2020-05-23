package au.gov.nla.imageanalysis.service;


import au.gov.nla.imageanalysis.config.ApplicationConfiguration;
import au.gov.nla.imageanalysis.enums.ServiceType;
import au.gov.nla.imageanalysis.logic.ImageLabel;
import au.gov.nla.imageanalysis.logic.ImageLabels;
import au.gov.nla.imageanalysis.logic.ServiceOutput;
import au.gov.nla.imageanalysis.util.*;

import com.amazonaws.util.IOUtils;
import com.google.common.net.HttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
     * This method processes an image with selected services and stores the returned JSON objects into a JSON array
     *
     * @param services  a list of String number, each number points to a cloud service.
     *                 eg, GL = google labeling service, AL = AWS labeling service ML = Microsoft azure labeling service
     *                     MD = Microsoft azure description services
     * @param pid the id of the image that's being processed by the cloud services
     * @return an instance of ServiceOutput storing the results
     */
    public ServiceOutput callImageServices(String pid, List<ServiceType> services){
        ServiceOutput serviceOutput = new ServiceOutput(pid, services);
        try{
            byte[] imageAsByteArray = IOUtils.toByteArray(HttpHelper.getAsStream(getUrl(pid)));
            serviceOutput.loadTestTargets(config.getTestLabelLocation());
            saveImage(imageAsByteArray);
            for (ServiceType serviceType : services){
                ImageLabels imageLabels = callImageService(imageAsByteArray, serviceType);
                List<ImageLabel> testTarget = serviceOutput.getTestTarget();
                if(testTarget.size()>0){
                    imageLabels.getEvaluation(testTarget);
                }
                serviceOutput.putImageServiceResult(serviceType, imageLabels);
            }
        }catch (IOException e){
            log.error("IOException: "+e.getMessage(),e);
        }
        return serviceOutput;
    }

    /**
     * This method processes images stored in a local folder, and write the result in a csv file, and to be downloaded from a browser
     * @param services list of services requested
     */
    public void processImages(List<ServiceType> services, HttpServletResponse response){
        List<String[]> resultList = new ArrayList<>();
        resultList.add(new ServiceOutput(services).csvTitles());
        try {
            Map<String,String> imageList = InOut.loadImagePathAsList(config.getTestImageLocation());
            int imageNumber = 0;
            int totalNumberOfImages = imageList.size();
            for(String pid: imageList.keySet()){
                ServiceOutput serviceOutput = new ServiceOutput(pid, services);
                byte[] imageAsByteArray = IOUtils.toByteArray(InOut.loadImage(imageList.get(pid)));
                serviceOutput.loadTestTargets(config.getTestLabelLocation());
                for (ServiceType serviceType : services){
                    ImageLabels imageLabels = callImageService(imageAsByteArray, serviceType);
                    List<ImageLabel> testTarget = serviceOutput.getTestTarget();
                    if(testTarget.size()>0){
                        imageLabels.getEvaluation(testTarget);
                    }
                    serviceOutput.putImageServiceResult(serviceType, imageLabels);
                }
                resultList.add(serviceOutput.toCustomizedCsvFormat());
                imageNumber+=1;
                log.info("Progress: {}/{},  Processing Image: {}.",imageNumber,totalNumberOfImages,pid);
                if(imageNumber==10){
                    break;
                }
            }
            exportCSV(resultList, response);
        }catch (IOException e){
            log.error("IOException: "+e.getMessage(),e);
        }
    }



    /**
     * The method checks which service is being called and then call the service
     * @param imageAsByteArray The byte array of the image being processed
     * @param serviceType The cloud service type that's being used to label the image
     * @return an instance of ImageLabels storing the results
     */

    public ImageLabels callImageService(byte [] imageAsByteArray, ServiceType serviceType){
        switch (serviceType){
            case GL:
                return googleImageService.googleImageLabeling(imageAsByteArray);
            case AL:
                return awsImageService.AWSImageLabeling(imageAsByteArray,config.getAWSAccessKey(),config.getAWSSecretKey());
            case ML:
                return azureImageService.azureImageLabeling(imageAsByteArray,config.getAzureAccessKey(),config.getAzureEndPoint());
            case MD:
                return azureImageService.azureImageDescription(imageAsByteArray,config.getAzureAccessKey(),config.getAzureEndPoint());
            default:
                log.info("Parameters contains: {}, it is not a available service.",ServiceType.MD.getDescription());
                return new ImageLabels(serviceType);
        }
    }

    /**
     * This method saves a image using byte array
     * @param imageAsByteArray the byte array of the image being saved
     */
    public void saveImage(byte[] imageAsByteArray) throws IOException{
        InOut.saveImage(imageAsByteArray,config.getImageSaveLocation());
    }

    public void exportCSV(List<String[]> resultList, HttpServletResponse response)throws IOException{
        response.reset();
        String filename = "result.csv";
        response.setContentType("text/csv");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\""+ filename+"\"");
        InOut.csvWriterOneByOne(resultList, response.getWriter());
    }
}
