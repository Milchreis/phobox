package de.milchreis.phobox.core;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import de.milchreis.phobox.core.file.FileProcessor;
import de.milchreis.phobox.core.file.filter.ImageFileFilter;
import de.milchreis.phobox.core.model.PhoboxModel;
import de.milchreis.phobox.core.model.SystemStatus;
import de.milchreis.phobox.db.repositories.ItemRepository;
import de.milchreis.phobox.utils.SpaceInfo;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PhoboxOperations {

	private PhoboxModel model;
	private ImageFileFilter fileFilter;
	
	public PhoboxOperations(PhoboxModel model) {
		this.model = model;
		fileFilter = new ImageFileFilter(PhoboxConfigs.SUPPORTED_VIEW_FORMATS);
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
		FileUtils.moveFileToDirectory(getThumb(file), getThumb(tar), true);
	}

	public void moveDir(File dir, File tar) throws IOException {
		FileUtils.moveDirectoryToDirectory(dir, tar, true);
		FileUtils.moveDirectoryToDirectory(getThumb(dir), getThumb(tar), true);
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
	
	public List<File> getNextFiles(int i, Iterator<File> it) {
		List<File> files = new ArrayList<File>();
		
		if(it == null) {
			it = FileUtils.iterateFiles(new File(model.getStoragePath()), null, true);
		}
		
		while(it.hasNext()) {
			File f = it.next();
			if(fileFilter.accept(f)) {
				files.add(f);

				if(i-- == -1) {
					return files;
				}
			}
		}
		
		return files;
	}

	
	public File getThumb(File image) {
		File thumbpath = model.getThumbPath();
		String imgPath = image.getAbsolutePath().replace(model.getStoragePath(), "");
		return new File(thumbpath, imgPath);
	}
	
	public File getPhysicalFile(File webfile) {
		return getPhysicalFile(webfile.getAbsolutePath());
	}
	
	public File getPhysicalFile(String webfile) {
		return new File(model.getStoragePath(), webfile);
	}
	
	public String getElementName(File element) {
		return FilenameUtils.getBaseName(element.getAbsolutePath());
	}

	public SystemStatus getStatus() {
		FileProcessor fileProcessor = Phobox.getImportProcessor();
		SystemStatus status = new SystemStatus();
		status.setImportStatus(fileProcessor.getStatus());
		status.setState(fileProcessor.getState());
		status.setFile(FilenameUtils.getName(fileProcessor.getCurrentfile()));
		status.setFreespace(SpaceInfo.getFreeSpaceMB(model.getStoragePath()));
		status.setMaxspace(SpaceInfo.getMaxSpaceMB(model.getStoragePath()));
		status.setRemainingfiles(getRemainingFiles().size());
		try {
			status.setNumberOfPictures(ItemRepository.getItemCount());
		} catch (SQLException | IOException e) {
			log.error("Error while counting number of images", e);
		}
		return status;
	}

	public String getWebPath(File file) {
		return getWebPath(file.toString());
	}
	
	public String getWebPath(String file) {
		String path = file;
		path = path.replace(model.getStoragePath(), "");
		path = path.replace(File.separatorChar, '/');		
		return path;
	}

	public boolean isInPhoboxDirectory(File file) {
		String path = getWebPath(file);
		return path.startsWith("/phobox/");
	}

}
