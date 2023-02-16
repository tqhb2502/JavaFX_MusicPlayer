package controller;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import main.MusicPlayer;

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
		
		try {
			
			// bind front track width with volume slider value
			frontVolumeTrack.prefWidthProperty().bind(volumeSlider.widthProperty().subtract(30).multiply(volumeSlider.valueProperty().divide(volumeSlider.maxProperty())));
			
			// bind volume label text width with volume slider value
			volumeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
				volumeLabel.setText(Integer.toString(newValue.intValue()));
			});
			
			// when the music player is muted, a click on volume slide can unmute the music player
			volumeSlider.setOnMousePressed(x -> {
				if (mutedButton.isVisible()) {
					muteClick();
				}
			});
			
		} catch (Exception ex) {

			ex.printStackTrace();
		}
	}
	
	public Slider getSlider() {
		return volumeSlider;
	}
	
	@FXML
	private void volumeClick() {
		MusicPlayer.getMainController().volumeClick();
	}

	@FXML
	private void muteClick() {
		
		// change UI
		PseudoClass muted = PseudoClass.getPseudoClass("muted");
		
		boolean isMuted = mutedButton.isVisible();
		muteButton.setVisible(isMuted);
		mutedButton.setVisible(!isMuted);
		
		volumeSlider.pseudoClassStateChanged(muted, !isMuted);
		frontVolumeTrack.pseudoClassStateChanged(muted, !isMuted);
		volumeLabel.pseudoClassStateChanged(muted, !isMuted);
		
		// change mute status of music player
		MusicPlayer.mute(isMuted);
	}
}
