package controller;

import java.net.URL;
import java.util.Collections;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import main.MusicPlayer;
import model.Artist;
import model.Library;
import model.Song;
import util.SubView;

public class ArtistsController implements Initializable, SubView {

	@FXML
	private FlowPane grid;

	/**
	 * Initializes the controller class.
	 */
	@Override
	public void initialize(URL url, ResourceBundle rb) {
		
		ObservableList<Artist> artists = Library.getArtists();
		Collections.sort(artists);
		
		int limit = (artists.size() < 25) ? artists.size() : 25;
		
		for (int i = 0; i < limit; i++) {
			Artist artist = artists.get(i);
			grid.getChildren().add(createCell(artist));
		}
		
		int rows = (artists.size() % 5 == 0) ? artists.size() / 5 : artists.size() / 5 + 1;
        grid.prefHeightProperty().bind(grid.widthProperty().divide(5).add(16).multiply(rows));
		
		new Thread(() -> {
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
			
			for (int j = 25; j < artists.size(); j++) {
				Artist artist = artists.get(j);
				Platform.runLater(() -> grid.getChildren().add(createCell(artist)));
			}
		}).start();
	}
	
	private VBox createCell(Artist artist) {
		
		// create artist's cell
		VBox cell = new VBox();
        Label title = new Label(artist.getTitle());
//        ImageView image = new ImageView(artist.getArtistImage());
//        image.imageProperty().bind(artist.artistImageProperty());
        VBox imageBox = new VBox();
		
		// title
		title.setTextOverrun(OverrunStyle.CLIP);
        title.setWrapText(true);
        title.setPadding(new Insets(10, 0, 10, 0));
        title.setAlignment(Pos.TOP_LEFT);
        title.setPrefHeight(66);
        title.prefWidthProperty().bind(grid.widthProperty().subtract(100).divide(5).subtract(1));
		
		// image box
		imageBox.prefWidthProperty().bind(grid.widthProperty().subtract(100).divide(5).subtract(1));
        imageBox.prefHeightProperty().bind(grid.widthProperty().subtract(100).divide(5).subtract(1));
        imageBox.setAlignment(Pos.CENTER);
		
		// add title and image box to cell, add style to this cell
		cell.getChildren().addAll(imageBox, title);
        cell.setPadding(new Insets(10, 10, 0, 10));
        cell.getStyleClass().add("artist-cell");
        cell.setAlignment(Pos.CENTER);
		
		// cell's event listener
		cell.setOnMouseClicked(event -> {
			System.out.println("artist cell clicked");
			MainController mainController = MusicPlayer.getMainController();
            ArtistsMainController artistsMainController = (ArtistsMainController) mainController.loadView("ArtistsMain");

            VBox artistCell = (VBox) event.getSource();
            String artistTitle = ((Label) artistCell.getChildren().get(1)).getText();
            Artist a = Library.getArtist(artistTitle);
            artistsMainController.selectArtist(a);
		});
		
		return cell;
	}

	@Override
	public void play() {
		
	}

	@Override
	public Song getSelectedSong() {
		return null;
	}
}
