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
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;

/**
 * FXML Controller class
 *
 * @author huytq
 */
public class VolumePopupController implements Initializable {

	@FXML
	private Pane mutedButton;
	@FXML
	private Pane muteButton;
	@FXML
	private Region backVolumeTrack;
	@FXML
	private Region frontVolumeTrack;
	@FXML
	private Slider volumeSlider;
	@FXML
	private Label volumeLabel;

	/**
	 * Initializes the controller class.
	 */
	@Override
	public void initialize(URL url, ResourceBundle rb) {
		// TODO
	}	

	@FXML
	private void muteClick(MouseEvent event) {
	}
	
}
