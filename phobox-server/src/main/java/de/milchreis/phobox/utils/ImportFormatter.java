package de.milchreis.phobox.utils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.drew.imaging.ImageProcessingException;

public class ImportFormatter {

	public static final String CREATION_YEAR = "%Y";
	public static final String CREATION_MONTH = "%M";
	public static final String CREATION_DAY = "%D";
	
	public static final SimpleDateFormat yearFormatter = new SimpleDateFormat("yyyy");
	public static final SimpleDateFormat monthFormatter = new SimpleDateFormat("MM");
	public static final SimpleDateFormat dayFormatter = new SimpleDateFormat("dd");
	
	private String importFormat;
	
	public ImportFormatter(String format) {
		importFormat = format;
	}
	
	public File createPath(File imageFile) throws ImageProcessingException, IOException {

		Date creationDate = ExifHelper.getCreationDate(imageFile);
		String subpath = "";
		
		for(String element : importFormat.split("/")) {
			element = element.replace("%Y", yearFormatter.format(creationDate));
			element = element.replace("%M", monthFormatter.format(creationDate));
			element = element.replace("%D", dayFormatter.format(creationDate));
			subpath += element + "/";
		}
		
		subpath = subpath.replace('/', File.separatorChar);
		return new File(subpath);
	}

	public boolean isValid() {
		if(importFormat == null || importFormat.isEmpty())
			return false;
		else
			return true;
	}
}
