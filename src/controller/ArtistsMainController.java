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
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;

/**
 * FXML Controller class
 *
 * @author huytq
 */
public class ArtistsMainController implements Initializable {

	@FXML
	private ScrollPane artistListScrollPane;
	@FXML
	private ListView<?> artistList;
	@FXML
	private ScrollPane scrollPane;
	@FXML
	private VBox subViewRoot;
	@FXML
	private Label artistLabel;
	@FXML
	private Separator separator;
	@FXML
	private Label albumLabel;
	@FXML
	private ListView<?> albumList;
	@FXML
	private TableView<?> songTable;
	@FXML
	private TableColumn<?, ?> playingColumn;
	@FXML
	private TableColumn<?, ?> titleColumn;
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
	
}
