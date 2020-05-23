package au.gov.nla.imageanalysis.controllers;


import au.gov.nla.imageanalysis.enums.ServiceType;
import au.gov.nla.imageanalysis.logic.ImageLabel;
import au.gov.nla.imageanalysis.logic.ImageLabels;
import au.gov.nla.imageanalysis.logic.ServiceOutput;
import au.gov.nla.imageanalysis.service.ImageService;

import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;


import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@SpringBootTest(classes = { ImageController.class })
class ImageControllerTest {

    @MockBean
    private ImageService imageService;

    @Autowired
    ImageController imageController;

    @Test
    void getImage() throws JSONException {
        String pid = "nla.obj-123";
        ImageLabels googleLabels = new ImageLabels(ServiceType.GL);
        googleLabels.addImageLabel(new ImageLabel("Photograph", 0.9539f));
        ServiceOutput serviceOutput = new ServiceOutput(pid,Arrays.asList(ServiceType.GL));
        serviceOutput.putImageServiceResult(ServiceType.GL,googleLabels);
        String expectedResult = "{\"service\":[{\"id\":\"GL\",\"labels\":[{\"label\":\"photograph\",\"relevance\":0.9539}]}],\"pid\":\"nla.obj-123\"}";

        when(imageService.callImageServices(pid,Arrays.asList(ServiceType.GL))).thenReturn(serviceOutput);
        String actualResult = imageController.getImage(pid, Arrays.asList(ServiceType.GL));

        verify(imageService).callImageServices(pid,Arrays.asList(ServiceType.GL));
        assertEquals(expectedResult, actualResult);
    }


}