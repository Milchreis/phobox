package de.milchreis.phobox.server.services;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.drew.imaging.ImageProcessingException;

import de.milchreis.phobox.core.Phobox;
import de.milchreis.phobox.core.PhoboxDefinitions;
import de.milchreis.phobox.core.PhoboxOperations;
import de.milchreis.phobox.core.model.PhoboxModel;
import de.milchreis.phobox.core.model.StorageItem;
import de.milchreis.phobox.db.entities.Item;
import de.milchreis.phobox.db.repositories.ItemRepository;
import de.milchreis.phobox.utils.storage.FilesystemHelper;
import de.milchreis.phobox.utils.phobox.ListHelper;
import de.milchreis.phobox.utils.exif.ExifHelper;

@Slf4j
@Service
public class PhotoService implements IPhotoService {
	
	@Autowired private ItemRepository itemRepository;
	
	@Override
	public StorageItem getItem(String webFilePath) {
		return getItem(Phobox.getOperations().getPhysicalFile(webFilePath));
	}

	@Override
	public StorageItem getItem(File physicalFile) {
		String webpath = Phobox.getOperations().getWebPath(physicalFile.getPath());
		return generateStorageItem(physicalFile, null);
	}

	@Override
	public StorageItem getItem(Item dbItem) {
		return generateStorageItem(null, dbItem);
	}

	@Override
	public Map<String, String> getExifData(String webFilePath) throws ImageProcessingException, IOException {
		return ExifHelper.getExifDataMap(Phobox.getOperations().getPhysicalFile(webFilePath));
	}

	@Override
	public void rename(String webItemPath, String targetname) throws Exception {
		File dir = Phobox.getOperations().getPhysicalFile(webItemPath);
		Phobox.getOperations().rename(dir, targetname);
	}
	
	@Override
	public void delete(String webItemPath) throws Exception {
		File dir = Phobox.getOperations().getPhysicalFile(webItemPath);
		Phobox.getOperations().delete(dir);
	}
	
	@Override
	public void move(String webItemPath, String targetDir) throws Exception {
		
		PhoboxOperations ops = Phobox.getOperations();
		File dir = ops.getPhysicalFile(webItemPath);
		File tar = ops.getPhysicalFile(targetDir);
		
		if(!tar.exists()) {
			tar.mkdirs();
		}
		
		if(dir.isFile() && tar.isDirectory()) {
			ops.moveFile(dir, tar);
		}
		
		if(dir.isDirectory() && tar.isDirectory()) {
			ops.moveDir(dir, tar);
		}
	}

	private boolean isValidItem(File physicalItem) {
		return (physicalItem.exists())
				|| physicalItem.isDirectory()
				|| (physicalItem.isFile() && ListHelper.endsWith(physicalItem.getName(), PhoboxDefinitions.SUPPORTED_VIEW_FORMATS));
	}

	private StorageItem generateStorageItem(File physicalFile, Item dbItem) {

		PhoboxOperations ops = Phobox.getOperations();
		PhoboxModel model = Phobox.getModel();

		if(dbItem == null && physicalFile == null)
			return null;

		if(physicalFile == null)
			physicalFile = ops.getPhysicalFile(dbItem.getFullPath());

		if(dbItem == null && physicalFile.isFile())
			dbItem = itemRepository.findByFullPath(ops.getWebPath(physicalFile));

		if(!isValidItem(physicalFile)) {
			return null;
		}

		String filename = physicalFile.getName();
		String directory = ops.getWebPath(physicalFile).replace(filename, "");

		String extention = FilenameUtils.getExtension(physicalFile.getName());
		extention = ListHelper.endsWith(extention.toLowerCase(), PhoboxDefinitions.SUPPORTED_VIEW_FORMATS) ? extention : "jpg";

		File thumbnail = ops.getThumb(new File(physicalFile.getParentFile(), FilenameUtils.getBaseName(physicalFile.getName()) + "." + extention));

		StorageItem storageItem = new StorageItem();

		storageItem.setName(FilenameUtils.getBaseName(physicalFile.getName()));
		storageItem.setPath(ops.getWebPath(new File(directory, physicalFile.getName())));
		storageItem.setType(physicalFile.isDirectory() ? StorageItem.TYPE_DIRECTORY : StorageItem.TYPE_FILE);

		File rawFile = FilesystemHelper.getRawIfExists(physicalFile, PhoboxDefinitions.SUPPORTED_RAW_FORMATS);
		if(rawFile != null) {
			File raw = new File(directory, rawFile.getName());
			storageItem.setRaw(ops.getWebPath(raw));
		}

		if(physicalFile.isDirectory()) {
			List<String> previewFiles = ops.getFiles(physicalFile, 1);
			if(previewFiles.size() >= 1) {

				storageItem.setPreview(ops.getStaticResourcePath(model.getThumbPath(), previewFiles.get(0)));
				File previewFile = new File(model.getStoragePath(), previewFiles.get(0));

				if(!ops.getThumb(previewFile).exists()) {

					// Add to database and create thumbnail
					Phobox.getEventRegistry().onNewFile(previewFile, null);

					// Set waiting icon
					storageItem.setGeneratingThumb(true);
				}
			}

		} else {

			storageItem.setThumb(ops.getStaticResourcePath(thumbnail));

			// Add landscape/portrait information by database item
			if(dbItem != null && Objects.nonNull(dbItem.getWidth()) && Objects.nonNull(dbItem.getHeight())) {
				storageItem.setLandscape(dbItem.getWidth() > dbItem.getHeight());
			}

			// Check existence of the thumbnails
			if(!thumbnail.exists()) {

				// Add to database and create thumbnail
				Phobox.getEventRegistry().onNewFile(physicalFile, null);

				// Set waiting icon
				storageItem.setGeneratingThumb(true);
			}
		}

		return storageItem;
	}
}
