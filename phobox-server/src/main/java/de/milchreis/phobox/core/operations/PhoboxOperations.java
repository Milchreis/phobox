package de.milchreis.phobox.core.operations;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.milchreis.phobox.core.Phobox;
import de.milchreis.phobox.core.PhoboxDefinitions;
import lombok.Getter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import de.milchreis.phobox.core.file.filter.ImageFileFilter;
import de.milchreis.phobox.core.model.PhoboxModel;
import de.milchreis.phobox.utils.storage.PathConverter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PhoboxOperations {

	private PhoboxModel model;
	private ImageFileFilter fileFilter;
	@Getter
	private ThumbnailOperations thumbnailOperations;
	
	public PhoboxOperations(PhoboxModel model) {
		this.model = model;
		this.thumbnailOperations = new ThumbnailOperations(model);
		fileFilter = new ImageFileFilter(PhoboxDefinitions.SUPPORTED_IMPORT_FORMATS);
	}
	
	public void rename(File dir, String targetname) throws Exception {
		log.debug(String.format(
				"Rename file %s to %s",
				dir.getAbsolutePath(),
				targetname));
		
		String endingOriginal = FilenameUtils.getExtension(dir.getName());
		if(!targetname.endsWith(endingOriginal)) {
			targetname = targetname + "." + endingOriginal;
		}
		
		String path = FilenameUtils.getFullPath(dir.getAbsolutePath());
		File correctedFile = new File(path, targetname);
		
		if(dir.isDirectory() || dir.isFile()) {
			if(correctedFile.exists()) {
				throw new Exception("File already exists");
			}
			
			dir.renameTo(correctedFile);
		}
		
		if(correctedFile.isFile()) {
			Phobox.getEventRegistry().onRenameFile(dir, correctedFile);
		}

		if(correctedFile.isDirectory()) {
			Phobox.getEventRegistry().onRenameDirectory(dir, correctedFile);
		}
	}

	public void moveFile(File file, File tar) throws IOException {
		FileUtils.moveFileToDirectory(file, tar, true);
		FileUtils.moveFileToDirectory(thumbnailOperations.getPhysicalThumbnail(file), thumbnailOperations.getPhysicalThumbnail(tar), true);
	}

	public void moveDir(File dir, File tar) throws IOException {
		FileUtils.moveDirectoryToDirectory(dir, tar, true);
		FileUtils.moveDirectoryToDirectory(thumbnailOperations.getPhysicalThumbnail(dir), thumbnailOperations.getPhysicalThumbnail(tar), true);
	}

	public List<String> getFiles(File directory, int number) {
		List<String> files = new ArrayList<String>();
		
		int counter = 0;
		Iterator<File> it = FileUtils.iterateFiles(directory, null, false);
		while (it.hasNext()) {
			File f = it.next();
			if(fileFilter.accept(f)) {
				if(counter > number && number != -1) {
					return files;
				} else {
					counter++;
					files.add(getWebPath(f));
				}
			}
		}
		
		return files;
	}
	
	public void delete(File item) throws IOException {
		log.debug(String.format("Delete file %s", item.getAbsolutePath()));
		
		if(item.isFile()) {
			item.delete();
			Phobox.getEventRegistry().onDeleteFile(item);
			
		} else if(item.isDirectory()) {
			FileUtils.deleteDirectory(item);
			Phobox.getEventRegistry().onDeleteDirectory(item);
		}
	}
	
	public List<String> getFiles(File directory) {
		return getFiles(directory, -1);
	}
	

	public List<String> getRemainingFiles() {
		return getFiles(model.getIncomingPath());
	}
	
	public String getStaticResourcePath(File path) {
		String webpath = getWebPath(path);
		webpath = webpath.startsWith("/") ? webpath : "/" + webpath;
		return "ext" + webpath;
	}
	
	public File getPhysicalFile(String webfile) {
		String webFilePath = PathConverter.decode(webfile);
		return new File(model.getStoragePath(), webFilePath);
	}
	
	public String getElementName(File element) {
		return FilenameUtils.getBaseName(element.getAbsolutePath());
	}

	public String getWebPath(File file) {
		return getWebPath(file.toString());
	}
	
	public String getWebPath(String file) {
		String path = file;

		if(model.getStoragePath() != null)
			path = path.replace(model.getStoragePath(), "");

		path = path.replace(File.separatorChar, '/');
		return path;
	}

	public boolean isInPhoboxDirectory(File file) {
		String path = getWebPath(file);
		return path.startsWith("/phobox/");
	}

}
