/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.TableCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.beans.value.ChangeListener;

import model.Song;
import main.MusicPlayer;
import controller.PlaylistsController;

/**
 *
 * @author Admin
 */
public class ControlPanelTableCell<S, T> extends TableCell<S, T>{
	private ChangeListener<Boolean> listener = (observable, oldValue, newValue) ->
			ControlPanelTableCell.this.updateItem(ControlPanelTableCell.this.getItem(), ControlPanelTableCell.this.isEmpty());
	
	@Override
	protected void updateItem(T item, boolean empty) {
		
		super.updateItem(item, empty);
		
		Song song = (Song) this.getTableRow().getItem();
//		
		if (empty || item == null || song == null) {
			setText(null);
			setGraphic(null);
		} else if (!song.getSelected()) {
			setText(item.toString());
			setGraphic(null);
			song.selectedProperty().removeListener(listener);
			song.selectedProperty().addListener(listener);
		} else {
			String fileName;
			// Selects the correct control panel based on whether the user is in a play list or not.
			if (MusicPlayer.getMainController().getSubViewController() instanceof PlaylistsController) {
				fileName = Resources.FXML + "ControlPanelPlaylists.fxml";
			} else {
				fileName = Resources.FXML + "ControlPanel.fxml";
			}
			try {
				Label text = new Label(item.toString());
				text.setTextOverrun(OverrunStyle.CLIP);
                FXMLLoader loader = new FXMLLoader(this.getClass().getResource(fileName));
                HBox controlPanel = loader.load();
                BorderPane cell = new BorderPane();
                cell.setRight(controlPanel);
                cell.setCenter(text);
                BorderPane.setAlignment(text, Pos.CENTER_LEFT);
                BorderPane.setAlignment(controlPanel, Pos.CENTER_LEFT);
                setText(null);
                setGraphic(cell);
                song.selectedProperty().removeListener(listener);
    			song.selectedProperty().addListener(listener);
            } catch (IOException ex) {
            }
		}
	}
}
