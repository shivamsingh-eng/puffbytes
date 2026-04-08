package com.puffbytes.puffbytes;

import com.puffbytes.puffbytes.upload.repository.MediaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
class PuffbytesApplicationTests {

	@MockitoBean
	private MediaRepository mediaRepository;

	@Test
	void contextLoads() {
	}

}
