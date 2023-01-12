/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import javafx.scene.control.OverrunStyle;
import javafx.scene.control.TableCell;

/**
 *
 * @author Admin
 */
public class ClippedTableCell<S, T> extends TableCell<S, T>{
	
	public ClippedTableCell() {
		setTextOverrun(OverrunStyle.CLIP);
	}
	
	protected void updateItem(T item, boolean empty) {
		super.updateItem(item, empty);

		if (empty || item == null) {
			setText(null);
			setGraphic(null);
		} else {
			setText(item.toString());
		}
 
	}
	
}
