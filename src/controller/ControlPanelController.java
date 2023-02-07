/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package controller;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.Transition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.util.Duration;
import main.MusicPlayer;
import model.Library;
import model.Playlist;
import model.Song;
import util.SubView;

/**
 * FXML Controller class
 *
 * @author huytq
 */
public class ControlPanelController implements Initializable {

	@FXML
	private HBox controlBox;
	@FXML
	private Pane playButton;
	@FXML
	private Pane playlistButton;
	
	private ContextMenu contextMenu;
	
	private Animation showMenuAnimation = new Transition() {
		{
			setCycleDuration(Duration.millis(250));
			setInterpolator(Interpolator.EASE_BOTH);
		}
		protected void interpolate(double frac) {
			contextMenu.setOpacity(frac);
		}
	};

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
	private void addToPlaylist(MouseEvent event) {
		double x = event.getScreenX();
		double y = event.getScreenY();

		// Retrieves the selected song to add to the desired playlist.
		Song selectedSong = MusicPlayer.getMainController().getSubViewController().getSelectedSong();

		ObservableList<Playlist> playlists = Library.getPlaylists();

		// Retrieves all the playlist titles to create menu items.
		ObservableList<String> playlistTitles = FXCollections.observableArrayList();
		for (Playlist playlist : playlists) {
			String title = playlist.getTitle();
			playlistTitles.add(title);
			
		}

		contextMenu = new ContextMenu();

		MenuItem playing = new MenuItem("Playing");
		playing.setStyle("-fx-text-fill: black");
		playing.setOnAction(e1 -> {
			MusicPlayer.addSongToNowPlayingList(selectedSong);
		});

		contextMenu.getItems().add(playing);

		if (playlistTitles.size() > 0) {
			SeparatorMenuItem item = new SeparatorMenuItem();
			item.getContent().setStyle(
					"-fx-border-width: 1 0 0 0; " +
							"-fx-border-color: #c2c2c2; " +
							"-fx-border-insets: 5 5 5 5;");
			contextMenu.getItems().add(item);
		}

		// Creates a menu item for each playlist title and adds it to the context menu.
		for (String title : playlistTitles) {
			MenuItem item = new MenuItem(title);
			item.setStyle("-fx-text-fill: black");

			item.setOnAction(e2 -> {
				// Finds the desired playlist and adds the currently selected song to it.
				String targetPlaylistTitle = item.getText();

				// Finds the correct playlist and adds the song to it.
				playlists.forEach(playlist -> {
					if (playlist.getTitle().equals(targetPlaylistTitle)) {
						playlist.addSong(selectedSong);
					}
				});
			});

			contextMenu.getItems().add(item);
		}

		contextMenu.setOpacity(0);
		contextMenu.show(playButton, x, y);
		showMenuAnimation.play();

		event.consume();
	}
	
}
