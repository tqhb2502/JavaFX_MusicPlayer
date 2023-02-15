package controller;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.MouseEvent;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import main.MusicPlayer;
import model.Library;
import util.ImportMusicTask;

public class ImportMusicDialogController implements Initializable {

	@FXML
	private Button importMusicButton;
	@FXML
	private ProgressBar progressBar;
	@FXML
	private Label label;
	
	private Stage dialogStage;
	private boolean musicImported = false;

	/**
	 * Initializes the controller class.
	 */
	@Override
	public void initialize(URL url, ResourceBundle rb) {
		// TODO
	}	
	
	public void setMusicImported(boolean musicImported) {
		this.musicImported = musicImported;
	}
	
	public void setDialogStage(Stage dialogStage) {
		this.dialogStage = dialogStage;
	}
	
	public boolean isMusicImported() {
		return musicImported;
	}

	@FXML
	private void handleImport(MouseEvent event) {
		
		try {
			
			// show file explorer
			DirectoryChooser directoryChooser = new DirectoryChooser();
			String musicDirectory = directoryChooser.showDialog(dialogStage).getPath();
			
			// create import music task
			ImportMusicTask<Boolean> task = new ImportMusicTask<Boolean>() {
				
				@Override
				protected Boolean call() throws Exception {
					
					try {
						musicImported = false;
						// delete old data
						Library.deleteLibraryXML();
						// get new data from new dir
						Library.importMusic(musicDirectory, this);
						return true;
					} catch (Exception e) {
						e.printStackTrace();
						return false;
					}
				}
			};
			
			// closes dialog when importing task succeeded
			task.setOnSucceeded(x -> {
				musicImported = true;
			    dialogStage.close();
			});
			
			// set task progress
			task.updateProgress(0, 1);
			
			// set progress bar to track task progress
			progressBar.progressProperty().bind(task.progressProperty());
			
			// create a thread to do importing task
			Thread thread = new Thread(task);
			thread.start();
			
			// display importing label
			label.setText("Importing music library...");
			
	        // Makes the import music button invisible and the progress bar visible.
	        // This happens as soon as the music import task is started.
        	importMusicButton.setVisible(false);
		    progressBar.setVisible(true);
			
		} catch (NullPointerException npe) {
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
