/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package controller;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import main.MusicPlayer;
import model.Library;
import util.SubView;
import util.XMLEditor;

/**
 * FXML Controller class
 *
 * @author huytq
 */
public class ControlPanelPlaylistsController implements Initializable {

	@FXML
	private HBox controlBox;
	@FXML
	private Pane playButton;
	@FXML
	private Pane deleteButton;

	/**
	 * Initializes the controller class.
	 */
	@Override
	public void initialize(URL url, ResourceBundle rb) {
		// TODO
	}	

	@FXML
	private void playSong(MouseEvent event) {
		SubView controller = MusicPlayer.getMainController().getSubViewController();
		controller.play();
		event.consume();
	}

	@FXML
	private void deleteSong(MouseEvent event) {
		PlaylistsController controller = (PlaylistsController) MusicPlayer.getMainController().getSubViewController();
		
		// Retrieves play list and song id to search for the song in the xml file.
		int selectedPlayListId = controller.getSelectedPlaylist().getId();
		int selectedSongId = controller.getSelectedSong().getId();
		
		// Calls methods to delete selected song from play list in XML file.
		XMLEditor.deleteSongFromPlaylist(selectedPlayListId, selectedSongId);

        // Removes the selected song from the playlist's song list in Library.
        Library.getPlaylist(selectedPlayListId).removeSong(selectedSongId);
		
		// Deletes the selected row from the table view.
		controller.deleteSelectedRow();
		
		event.consume();
	}
	
}
