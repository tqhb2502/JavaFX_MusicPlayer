package controller;

import java.io.IOException;
import model.Player;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ResourceBundle;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.Transition;
import javafx.css.PseudoClass;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import main.MusicPlayer;
import model.Song;
import util.Resources;

public class MainController implements Initializable {
	
	private boolean isSideBarExpanded = true;
    private double expandedWidth = 250;
    private double collapsedWidth = 50;
    private double expandedHeight = 50;
    private double collapsedHeight = 0;
	private double searchExpanded = 180;
	private double searchCollapsed = 0;
	
	private CountDownLatch viewLoadedLatch; // use to synchronize threads
	private Stage volumePopup;
    private Stage searchPopup;
    private VolumePopupController volumePopupController;
	
	/**
	 * FXML nodes
	 */
	@FXML
	private BorderPane mainWindow;
	@FXML
	private VBox sideBar;
	@FXML
	private ImageView sideBarSlideButton;
	@FXML
	private TextField searchBox;
	@FXML
	private VBox playlistBox;
	@FXML
	private ImageView nowPlayingArtwork;
	@FXML
	private Label nowPlayingTitle;
	@FXML
	private Label nowPlayingArtist;
	@FXML
	private Label timePassed;
	@FXML
	private Region backSliderTrack;
	@FXML
	private Region frontSliderTrack;
	@FXML
	private Slider timeSlider;
	@FXML
	private Label timeRemaining;
	@FXML
	private HBox controlBox;
	@FXML
	private Pane backButton;
	@FXML
	private Pane playButton;
	@FXML
	private Pane pauseButton;
	@FXML
	private Pane skipButton;
	@FXML
	private Pane loopButton;
	@FXML
	private Pane shuffleButton;
	@FXML
	private Pane volumeButton;
	@FXML
	private HBox letterBox;
	@FXML
	private Separator letterSeparator;
	@FXML
	private ScrollPane subViewRoot;

	/**
	 * Initializes the controller class.
	 */
	@Override
	public void initialize(URL url, ResourceBundle rb) {
		
		// prepare latch
		resetViewLoadedLatch();
		
		// remove pause button
		controlBox.getChildren().remove(2);
		
		// make frontSliderTrack move along timeSlider
		frontSliderTrack.prefWidthProperty().bind(timeSlider.widthProperty().multiply(timeSlider.valueProperty().divide(timeSlider.maxProperty())));
		
		// popups
		createVolumePopup();
		createSearchPopup();
		
		// loop & shuffle
		PseudoClass active = PseudoClass.getPseudoClass("active");
		
		loopButton.setOnMouseClicked(event -> {
			
			sideBar.requestFocus();
			MusicPlayer.toggleLoop();
			loopButton.pseudoClassStateChanged(active, MusicPlayer.isLoopActive());
		});
		shuffleButton.setOnMouseClicked(event -> {
			
			sideBar.requestFocus();
			MusicPlayer.toggleShuffle();
			shuffleButton.pseudoClassStateChanged(active, MusicPlayer.isShuffleActive());
		});
		
		// time slider value change
		timeSlider.setFocusTraversable(false);
		
		timeSlider.valueChangingProperty().addListener((slider, wasChanging, isChanging) -> {
			
			if (wasChanging) {
				
				int seconds = (int) Math.round(timeSlider.getValue() / 4.0);
				
				timeSlider.setValue(seconds * 4);
				MusicPlayer.seek(seconds);
			}
		});
		
		timeSlider.valueProperty().addListener((slider, oldValue, newValue) -> {
			
			double previous = oldValue.doubleValue();
			double current = newValue.doubleValue();
			
			if (!timeSlider.isValueChanging() && current != previous + 1) {
				
				int seconds = (int) Math.round(current / 4.0);
				
				timeSlider.setValue(seconds * 4);
				MusicPlayer.seek(seconds);
			}
		});
		
		// displays now playing song's info
		updateNowPlayingButton();
		initializeTimeLabels();
		initializeTimeSlider();
		//playlist
		
		// loads default sub view
	}
	
	private void resetViewLoadedLatch() {
		viewLoadedLatch = new CountDownLatch(1);
	}
	
	public CountDownLatch getViewLoadedLatch() {
		return viewLoadedLatch;
	}

	/**
	 * Side bar 
	 */
	@FXML
	private void slideSideBar(Event e) {
		sideBar.requestFocus();
		searchBox.setText("");
		if (isSideBarExpanded) {
			collapseSideBar();
		} else {
			expandSideBar();
		}
	}
	
	private void collapseSideBar() {
		if (expandAnimation.statusProperty().get() == Animation.Status.STOPPED
			&& collapseAnimation.statusProperty().get() == Animation.Status.STOPPED) {
			
			collapseAnimation.play();
		}
	}
	
	private void expandSideBar() {
		if (expandAnimation.statusProperty().get() == Animation.Status.STOPPED
			&& collapseAnimation.statusProperty().get() == Animation.Status.STOPPED) {
			
			expandAnimation.play();
		}
	}
	
	private void setSlideDirection() {
		isSideBarExpanded = !isSideBarExpanded;
	}

	/**
	 * Popups
	 */
	private void createVolumePopup() {
		try {
			
			Stage stage = MusicPlayer.getStage();
			
			FXMLLoader loader = new FXMLLoader(this.getClass().getResource(Resources.FXML + "VolumePopup.fxml"));
			Parent view = loader.load();
			volumePopupController = loader.getController();
			
			Stage popup = new Stage();
			popup.setScene(new Scene(view));
			popup.initStyle(StageStyle.UNDECORATED);
        	popup.initOwner(stage);
        	popup.setX(stage.getWidth() - 270);
        	popup.setY(stage.getHeight() - 120);
			popup.focusedProperty().addListener((event, wasFocused, isFocused) -> {
				if (wasFocused && !isFocused) {
					volumeHideAnimation.play();
				}
			});
			
			popup.show();
			popup.hide();
			volumePopup = popup;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void createSearchPopup() {
		try {
			
			Stage stage = MusicPlayer.getStage();
			
			VBox view = new VBox();
			view.getStylesheets().add(Resources.CSS + "MainStyle.css");
			view.getStyleClass().add("searchPopup");
			
			Stage popup = new Stage();
			popup.setScene(new Scene(view));
			popup.initStyle(StageStyle.UNDECORATED);
			popup.initOwner(stage);
			
			popup.show();
			popup.hide();
			searchPopup = popup;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void updateNowPlayingButton() {
		
		Song song = MusicPlayer.getNowPlaying();
		
		if (song != null) {
			nowPlayingTitle.setText(song.getTitle());
			nowPlayingArtist.setText(song.getArtist());
			nowPlayingArtwork.setImage(null);
		} else {
			nowPlayingTitle.setText("");
			nowPlayingArtist.setText("");
			nowPlayingArtwork.setImage(null);
		}
	}
	
	public void initializeTimeSlider() {
		
		Song song = MusicPlayer.getNowPlaying();
		
		if (song != null) {
			timeSlider.setMin(0);
            timeSlider.setMax(song.getLengthInSeconds() * 4);
            timeSlider.setValue(0);
            timeSlider.setBlockIncrement(1);
		} else {
			timeSlider.setMin(0);
            timeSlider.setMax(1);
            timeSlider.setValue(0);
            timeSlider.setBlockIncrement(1);
		}
	}
	
	public void updateTimeSlider() {
		timeSlider.increment();
	}
	
	public boolean isTimeSliderPressed() {
		return timeSlider.isPressed();
	}
	
	public void initializeTimeLabels() {
		
		Song song = MusicPlayer.getNowPlaying();
		
		if (song != null) {
			timePassed.setText("0:00");
			timeRemaining.setText(song.getLength());
		} else {
			timePassed.setText("");
			timeRemaining.setText("");
		}
	}

	public void updateTimeLabels() {
		timePassed.setText(MusicPlayer.getTimePassed());
		timeRemaining.setText(MusicPlayer.getTimeRemaining());
	}
	
	/**
	 * Change between play icon and pause icon
	 * @param isPlaying: true -> pause icon, false -> play icon 
	 */
	public void updatePlayPauseIcon(boolean isPlaying) {
		
		controlBox.getChildren().remove(1);
		
		if (isPlaying) {
			controlBox.getChildren().add(1, pauseButton);
		} else {
			controlBox.getChildren().add(1, playButton);
		}
	}
	
	@FXML
	private void selectView(Event e) {
	}

	@FXML
	private void newPlaylist(Event e) {
	}

	@FXML
	private void navigateToCurrentSong(Event e) {
	}

	@FXML
	private void back(Event e) {
		
		sideBar.requestFocus();
		MusicPlayer.back();
	}

	@FXML
	private void playPause(Event e) {
		
		sideBar.requestFocus();
    	
        if (MusicPlayer.isPlaying()) {
            MusicPlayer.pause();
        } else {
            MusicPlayer.play();
        }
	}

	@FXML
	private void skip(Event e) {
		
		sideBar.requestFocus();
		MusicPlayer.skip();
	}

	@FXML
	private void volumeClick(Event e) {
		if (!volumePopup.isShowing()) {
			Stage stage = MusicPlayer.getStage();
    		volumePopup.setX(stage.getX() + stage.getWidth() - 265);
        	volumePopup.setY(stage.getY() + stage.getHeight() - 115);
    		volumePopup.show();
    		volumeShowAnimation.play();
		}
	}

	@FXML
	private void letterClicked(Event e) {
	}
	
	/**
	 * Animation
	 */
	private Animation collapseAnimation = new Transition() {
		// This is a anonymous class
		
		// Initialize instance members
		{
			setCycleDuration(Duration.millis(250));
			setInterpolator(Interpolator.EASE_BOTH);
			setOnFinished(x -> setSlideDirection()); // lambda expression
		}
		
		@Override
		protected void interpolate(double frac) {
			// frac: 0.0 -> 1.0
			
			double curWidth = collapsedWidth + (expandedWidth - collapsedWidth) * (1.0 - frac);
			double searchWidth = searchCollapsed + (searchExpanded - searchCollapsed) * (1.0 - frac);
			
			sideBar.setPrefWidth(curWidth);
			searchBox.setPrefWidth(searchWidth);
			searchBox.setOpacity(1.0 - frac);
		}
	};
	
	private Animation expandAnimation = new Transition() {
		
		{
			setCycleDuration(Duration.millis(250));
			setInterpolator(Interpolator.EASE_BOTH);
			setOnFinished(x -> setSlideDirection());
		}
		
		@Override
		protected void interpolate(double frac) {
			
			double curWidth = collapsedWidth + (expandedWidth - collapsedWidth) * (frac);
			double searchWidth = searchCollapsed + (searchExpanded - searchCollapsed) * (frac);
			
			sideBar.setPrefWidth(curWidth);
			searchBox.setPrefWidth(searchWidth);
			searchBox.setOpacity(frac);
		}
	};
	
	private Animation volumeShowAnimation = new Transition() {
    	{
            setCycleDuration(Duration.millis(250));
            setInterpolator(Interpolator.EASE_BOTH);
        }
        protected void interpolate(double frac) {
            volumePopup.setOpacity(frac);
        }
    };
	
	private Animation volumeHideAnimation = new Transition() {
    	{
            setCycleDuration(Duration.millis(250));
            setInterpolator(Interpolator.EASE_BOTH);
			setOnFinished(x -> volumePopup.hide());
        }
        protected void interpolate(double frac) {
            volumePopup.setOpacity(1.0 - frac);
        }
    };
	
	private Animation searchShowAnimation = new Transition() {
		{
			setCycleDuration(Duration.millis(250));
            setInterpolator(Interpolator.EASE_BOTH);
		}
		@Override
		protected void interpolate(double frac) {
			searchPopup.setOpacity(frac);
		}
	};
		
	private Animation searchHideAnimation = new Transition() {
		{
			setCycleDuration(Duration.millis(250));
            setInterpolator(Interpolator.EASE_BOTH);
			setOnFinished(x -> searchPopup.hide());
		}
		@Override
		protected void interpolate(double frac) {
			searchPopup.setOpacity(1.0 - frac);
		}
	};
}