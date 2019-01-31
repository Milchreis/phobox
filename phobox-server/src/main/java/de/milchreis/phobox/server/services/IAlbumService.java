package de.milchreis.phobox.server.services;

import java.io.IOException;
import java.util.List;

import de.milchreis.phobox.core.model.StorageAlbum;

import javax.servlet.http.HttpServletResponse;

public interface IAlbumService {

	List<String> getAllAlbumNames();
	
	StorageAlbum getStorageAlbumsByName(String albumname);
	
	void addItemToAlbum(String albumName, String itemPath);

    void downloadAlbumAsZip(String albumname, HttpServletResponse response) throws IOException;
}
