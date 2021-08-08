package au.gov.nla.imageanalysis;

import au.gov.nla.imageanalysis.controllers.ImageController;
import au.gov.nla.imageanalysis.service.ImageService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest(classes = { ImageController.class })
class ImageAnalysisApplicationTests {

	@MockBean
	private ImageService imageService;

	@Test
	void contextLoads() {
	}

}
