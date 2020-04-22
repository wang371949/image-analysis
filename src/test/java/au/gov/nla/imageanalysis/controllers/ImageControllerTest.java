package au.gov.nla.imageanalysis.controllers;


import au.gov.nla.imageanalysis.service.ImageService;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.io.IOException;
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
        String  url = "https://trove.nla.gov.au/proxy?url=http://nla.gov.au/nla.obj-159043847-t&md5=O6N-K5SwjBH2ApTGObbxvA&expires=1587996000";
        String googleImageLabelingResponse = "{\"id\":\"1\", \"labels\":[{\"label\":\"Photograph\", \"relevance\": 0.9539}]}";
        String expectedResult = "{\"service\":[{\"id\":\"1\",\"labels\":[{\"label\":\"Photograph\",\"relevance\":0.9539}]}],\"pid\":\"nla.obj-123\"}";

        when(imageService.googleImageLabeling(anyString())).thenReturn(new JSONObject(googleImageLabelingResponse));
        String actualResult = imageController.getImage(pid, Arrays.asList("1"));

        verify(imageService).googleImageLabeling(url);
        assertEquals(expectedResult, actualResult);
    }
}