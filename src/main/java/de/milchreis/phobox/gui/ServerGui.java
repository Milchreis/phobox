package de.milchreis.phobox.gui;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.SocketException;
import java.net.URI;
import java.net.URL;
import java.util.ResourceBundle;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.imgscalr.Scalr;
import org.imgscalr.Scalr.Rotation;

import de.milchreis.phobox.core.Phobox;
import de.milchreis.phobox.core.PhoboxConfigs;
import de.milchreis.phobox.core.config.PreferencesManager;
import de.milchreis.phobox.core.file.FileAction;
import de.milchreis.phobox.core.file.FileProcessor;
import de.milchreis.phobox.core.file.LoopInfo;
import de.milchreis.phobox.core.model.PhoboxModel;
import de.milchreis.phobox.utils.LocalIpAddress;
import de.milchreis.phobox.utils.SwingFXUtils;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;


public class ServerGui extends Application implements Initializable {
	private static final Logger log = Logger.getLogger(ServerGui.class);
	
	public static void scale(File original, File target, int sizeW, int sizeH) throws IOException {
		Image image = new Image(original.toURI().toString(), sizeW, sizeH, true, false);
		String format = target.getAbsolutePath().substring(target.getAbsolutePath().length() - 3).toLowerCase();
		ImageIO.write(SwingFXUtils.fromFXImage(image, null), format, target);
	}
	
	public static void rotate(File original, File target, Rotation degree) throws IOException {
		Image image = new Image(original.toURI().toString());
		BufferedImage img = SwingFXUtils.fromFXImage(image, null);
		
		if(degree != null) {
			img = Scalr.rotate(img, degree, Scalr.OP_ANTIALIAS);
		}
		
		if(img != null) {
			String format = target.getAbsolutePath().substring(target.getAbsolutePath().length() - 3).toLowerCase();
			ImageIO.write(img, format, target);
		}
	}

	// ---- GUI -----------------------------------------------------------------------------------------------------
	
	private Stage stage;
	private @FXML Label addressLabel;
	private @FXML Button storageButton;
	
    @Override
    public void start(Stage primaryStage) {
    	this.stage = primaryStage;
    	if(Phobox.getModel().isActiveGui()) {
    		try {
    			initGui();
			} catch (Exception e) {
				log.error("Error while loading GUI", e);
			}
    	}
    }
    
    public void initGui() throws Exception {
    	URI uri = ServerGui.class.getClassLoader().getResource("PhoboxServerGui.fxml").toURI();
    	URL url = uri.toURL();
    	
    	Parent root = FXMLLoader.<Parent>load(url);
    	Scene scene = new Scene(root, 500, 370);
    	
    	stage.getIcons().add(new Image(getClass().getResourceAsStream("/WebContent/img/favicon_64.png")));
    	stage.setOnCloseRequest(e -> onClose());
    	stage.setTitle("Phobox");
    	stage.setScene(scene);
    	stage.show();
    }
    
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		try {
			if(LocalIpAddress.getLocalIP().size() > 0) {
				addressLabel.setText("http://"+LocalIpAddress.getLocalIP().get(0)+":"+Phobox.getModel().getPort());
			}
		} catch (SocketException e) {
			log.error("Error while loading local address", e);
		}
	}
	
	@FXML
	protected void onChangeStoragePath() {
		DirectoryChooser fileChooser = new DirectoryChooser();
		File file = fileChooser.showDialog(stage);
		if (file != null) {
			String path = file.getAbsolutePath();
			storageButton.setText(path);
			PreferencesManager.set(PreferencesManager.STORAGE_PATH, path);
			Phobox.getModel().setStoragePath(path);
		}
	}
	
	@FXML
	protected void onDrag(DragEvent event) {
		Dragboard db = event.getDragboard();
		if (db.hasFiles()) {
			event.acceptTransferModes(TransferMode.COPY);
		} else {
			event.consume();
		}
	}
	
	@FXML
	protected void onDrop(DragEvent event){
		Dragboard db = event.getDragboard();
		boolean success = false;
		if (db.hasFiles()) {
			final File incomingPath = Phobox.getModel().getIncomingPath();

			success = true;
			String filePath = null;

			for (File file : db.getFiles()) {
				if (file.isDirectory()) {
					new FileProcessor().foreachFile(file, PhoboxConfigs.SUPPORTED_IMPORT_FORMATS, new FileAction() {
						@Override
						public void process(File file, LoopInfo info) {
							try {
								FileUtils.copyFileToDirectory(file, incomingPath);
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					});
				} else {
					try {
						filePath = file.getAbsolutePath();
						System.out.println(filePath);

						FileUtils.copyFileToDirectory(file, incomingPath);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		event.setDropCompleted(success);
		event.consume();
	}
	
	@FXML
	protected void onOpenGUI() {
		PhoboxModel model = Phobox.getModel();
		getHostServices().showDocument("http://localhost:"+model.getPort());
	}
	
	@FXML
	protected void onClose() {
		System.exit(0);
	}
}
