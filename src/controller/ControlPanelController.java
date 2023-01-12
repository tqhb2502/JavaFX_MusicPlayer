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
	}
	
}
