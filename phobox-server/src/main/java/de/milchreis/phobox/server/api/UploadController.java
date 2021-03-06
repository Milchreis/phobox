package de.milchreis.phobox.server.api;

import de.milchreis.phobox.server.services.IFileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/upload")
public class UploadController {

	@Autowired private IFileStorageService fileStorageService;

	@RequestMapping(value = "", method = RequestMethod.POST)
	public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file) {
		
		try {
			fileStorageService.storeFile(file);
			return new ResponseEntity<>(HttpStatus.CREATED);
		} catch (Exception e) {
			log.debug("file store exception", e);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

}
