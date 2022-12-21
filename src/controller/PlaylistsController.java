/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package controller;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

/**
 * FXML Controller class
 *
 * @author huytq
 */
public class PlaylistsController implements Initializable {

	@FXML
	private Label playlistTitleLabel;
	@FXML
	private HBox controlBox;
	@FXML
	private Pane playButton;
	@FXML
	private Pane deleteButton;
	@FXML
	private TableView<?> tableView;
	@FXML
	private TableColumn<?, ?> playingColumn;
	@FXML
	private TableColumn<?, ?> titleColumn;
	@FXML
	private TableColumn<?, ?> artistColumn;
	@FXML
	private TableColumn<?, ?> albumColumn;
	@FXML
	private TableColumn<?, ?> lengthColumn;
	@FXML
	private TableColumn<?, ?> playsColumn;

	/**
	 * Initializes the controller class.
	 */
	@Override
	public void initialize(URL url, ResourceBundle rb) {
		// TODO
	}	

	@FXML
	private void playPlaylist(MouseEvent event) {
	}

	@FXML
	private void deletePlaylist(MouseEvent event) {
	}
	
}
