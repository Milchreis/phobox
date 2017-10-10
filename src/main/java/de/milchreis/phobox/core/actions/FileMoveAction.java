package de.milchreis.phobox.core.actions;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import org.apache.commons.io.FileExistsException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.log4j.Logger;

import com.drew.imaging.ImageProcessingException;

import de.milchreis.phobox.core.Phobox;
import de.milchreis.phobox.core.config.ConfigManager;
import de.milchreis.phobox.core.file.FileAction;
import de.milchreis.phobox.core.file.LoopInfo;
import de.milchreis.phobox.core.model.PhoboxModel;
import de.milchreis.phobox.utils.ImportFormatter;
import de.milchreis.phobox.utils.ListHelper;

public class FileMoveAction implements FileAction {
	private static final Logger log = Logger.getLogger(FileMoveAction.class);
	
	public static String[] MOVIE_FORMATS = {"mp4", "mov", "avi"};
	
	@Override
	public void process(File file, LoopInfo info) {
		
		PhoboxModel model = Phobox.getModel();
		File storage = new File(model.getStoragePath());
		
		if(ListHelper.endsWith(file.getName(), MOVIE_FORMATS)) {
			File movies = new File(storage, ConfigManager.get(ConfigManager.STORAGE_MOVIES));
			silentMove(file, movies, false);
			return;
		}
		
		try {
			ImportFormatter importFormatter = new ImportFormatter(model.getImportFormat());
			File dirStructure = importFormatter.createPath(file);
			File target = new File(storage, dirStructure.toString());
			
			if(!silentMove(file, target, true)) {
				File doubles = new File(storage, ConfigManager.get(ConfigManager.STORAGE_DOUBLES));
				silentMove(file, doubles, false);
			}
			
			return;
			
		} catch (ImageProcessingException | NullPointerException e) {
			log.warn("Could not read exifdata", e);
			
		} catch (IOException e1) {
			log.warn("Could not create", e1);
		} 
		
		// Move file to unsorted
		File target = new File(
				storage, 
				ConfigManager.get(ConfigManager.STORAGE_UNSORTED));
		
		silentMove(file, target, false);
	}
	
	private boolean silentMove(File source, File target, boolean checkExistence) {
		try {
			
			if(checkExistence) {
				File tar = existsRenamedDirectory(target);
				if(tar != null) {
					target = tar;
				}
				
				File targetFile = new File(target, FilenameUtils.getName(source.getAbsolutePath()));
				if(targetFile.exists() && FileUtils.contentEquals(source, targetFile)) {
					log.info("File already stored. It will be removed: " + source.getAbsolutePath());
					source.delete();
					return true;
				} 
			}
			
			FileUtils.moveFileToDirectory(source, target, true);
			return true;

		} catch(FileExistsException e) {
			source.delete();
		} catch(Exception e) {
			log.warn("Could not move File: " + e.getLocalizedMessage());
		}
		
		return false;
	}
	
	private File existsRenamedDirectory(File target) {
		File parent = target.getParentFile();
		if(parent.exists()) {
			File[] subdirs = parent.listFiles((FileFilter) DirectoryFileFilter.DIRECTORY);
			for (File dir : subdirs) {
				if(dir.getAbsolutePath().startsWith(target.getAbsolutePath())) {
					return dir;
				}
			}
		}
			
		
		return null;
	}
}
