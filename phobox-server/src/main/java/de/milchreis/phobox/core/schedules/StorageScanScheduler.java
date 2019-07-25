package de.milchreis.phobox.core.schedules;

import java.io.File;
import java.util.*;

import de.milchreis.phobox.core.Phobox;
import de.milchreis.phobox.core.PhoboxDefinitions;
import de.milchreis.phobox.core.file.FileAction;
import de.milchreis.phobox.core.file.FileProcessor;
import de.milchreis.phobox.core.file.LoopInfo;
import de.milchreis.phobox.db.repositories.ItemRepository;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.internal.util.StopWatch;

@Slf4j
public class StorageScanScheduler extends TimerTask implements FileAction, Schedulable {
	
	public static final int IMMEDIATELY = 0;
	
	private boolean recursive;
	private Timer timer;
	private int startingDelayInHours;
	private Integer startingHour;
	private Integer startingMinute;
	private File directory;
	private boolean ready;
	private ItemRepository itemRepository;
	private File currentFile;
	private FileProcessor fileProcessor;
	
	
	public StorageScanScheduler(int startingDelayInHours) {
		timer = new Timer();
		this.setStartingDelayInHours(startingDelayInHours);
		recursive = true;
		fileProcessor = new FileProcessor();
	}
	
	public StorageScanScheduler(int startingDelayInHours, File directory, ItemRepository itemRepository, boolean recursive) {
		this(null, null, startingDelayInHours, directory, itemRepository, recursive);
	}

	public StorageScanScheduler(Integer startingHour, Integer startingMinute, int startingDelayInHours, File directory, ItemRepository itemRepository, boolean recursive) {
		this(startingDelayInHours);
		this.directory = directory;
		this.startingHour = startingHour;
		this.startingMinute = startingMinute;
		this.recursive = recursive;
		this.itemRepository = itemRepository;
	}

	
	@Override
	public void run() {
		log.info("Start storage scanner");
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();

		if(directory == null) {
			directory = new File(Phobox.getModel().getStoragePath());
		}

		fileProcessor.foreachFile(
				directory, 
				PhoboxDefinitions.SUPPORTED_IMPORT_FORMATS,
				Arrays.asList(this),
				recursive,
				0);
		
		// Check the database
		itemRepository.findAllAsIteratable().forEach(item -> {
			
			currentFile = Phobox.getOperations().getPhysicalFile(item.getFullPath());
			
			if(!currentFile.exists()) {
				// Start Registry for onDelete event -> removes item from database
				// and deletes thumbnail if exists
				Phobox.getEventRegistry().onDeleteFile(currentFile);
			}
		});

		stopWatch.stop();
		log.info("StorageScan needed " + stopWatch.getTotalTimeMillis() / 1000.0f + " seconds");

		ready = true;
	}
	
	@Override
	public void process(File file, LoopInfo info) {
		// Skip the phobox directory
		if(Phobox.getOperations().isInPhoboxDirectory(file)) {
			return;
		}
		
		// Crawling over the storage and find missing database entries and thumbnails
		// The magic is happened by the registered events
		Phobox.getEventRegistry().onCheckExistingFile(file, null);
	}

	@Override
	public void start() {
		if (startingDelayInHours == IMMEDIATELY) {
			timer.schedule(this, 1);

		} else if(startingHour != null && startingMinute != null) {
			timer.schedule(this, getStartDate(), getStartingDelayInHours() * 3600000);
		}
	}

	@Override
	public void stop() {
		timer.cancel();
	}

	@Override
	public boolean isReady() {
		return ready;
	}

	public int getStartingDelayInHours() {
		return startingDelayInHours;
	}

	public void setStartingDelayInHours(int startingDelayInHours) {
		this.startingDelayInHours = startingDelayInHours;
	}
	
	private Date getStartDate() {
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(new Date());
		cal.set(Calendar.HOUR_OF_DAY, startingHour);
		cal.set(Calendar.MINUTE, startingMinute);
		return cal.getTime();
	}

	public File getCurrentFile() {
		String storageFile = fileProcessor.getCurrentfile();
		File dbFile = currentFile;
		return storageFile == null || storageFile.isEmpty() ? dbFile : new File(storageFile);
	}
	
}
