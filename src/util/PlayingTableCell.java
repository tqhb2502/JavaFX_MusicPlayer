/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TableCell;
import javafx.scene.layout.Pane;

/**
 *
 * @author Admin
 */
public class PlayingTableCell<S, T> extends TableCell<S, T>{
	
	protected void updateItem(T item, Boolean empty) {
		super.updateItem(item, empty);
		if (empty || item == null || !(Boolean) item) {
			setText(null);
			setGraphic(null);
		} else {
			try {
				String fileName = Resources.FXML + "PlayingIcon.fxml";
                FXMLLoader loader = new FXMLLoader(this.getClass().getResource(fileName));
                Pane pane = loader.load();
                setGraphic(pane);
            } catch (IOException ex) {
            }
		}
	}
}
