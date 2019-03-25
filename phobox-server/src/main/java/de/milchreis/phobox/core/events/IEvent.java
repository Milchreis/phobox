package de.milchreis.phobox.core.events;

import java.io.File;

public interface IEvent {

	void onCreation();
	void onStop();
	
	void onNewFile(File incomingfile, EventLoopInfo eventLoopInfo);
	void onDeleteFile(File file);
	void onDeleteDirectory(File directory);
	void onRenameFile(File original, File newFile);
	void onRenameDirectory(File directory, File newDirectory);

}
