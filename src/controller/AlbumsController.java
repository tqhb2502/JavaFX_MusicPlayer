package controller;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import model.Song;

public class AlbumsController implements Initializable {

	@FXML
	private ScrollPane gridBox;
	@FXML
	private FlowPane grid;
	@FXML
	private VBox songBox;
	@FXML
	private Separator horizontalSeparator;
	@FXML
	private Label artistLabel;
	@FXML
	private Separator verticalSeparator;
	@FXML
	private Label albumLabel;
	@FXML
	private TableView<Song> songTable;
	@FXML
	private TableColumn<Song, Boolean> playingColumn;
	@FXML
	private TableColumn<Song, String> titleColumn;
	@FXML
	private TableColumn<Song, String> lengthColumn;
	@FXML
	private TableColumn<Song, Integer> playsColumn;

	/**
	 * Initializes the controller class.
	 */
	@Override
	public void initialize(URL url, ResourceBundle rb) {
		// TODO
	}	
	
}
