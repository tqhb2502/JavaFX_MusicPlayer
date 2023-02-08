package controller;

import java.io.IOException;
import model.Player;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.Animation;
import javafx.animation.Animation.Status;
import javafx.animation.Interpolator;
import javafx.animation.Transition;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.css.PseudoClass;
import javafx.event.Event;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import main.MusicPlayer;
import model.Library;
import model.Playlist;
import model.SearchResult;
import model.Song;
import util.Resources;
import util.Search;
import util.SubView;

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
	@FXML
	private HBox artistsHBox;
	@FXML
	private HBox albumsHBox;
	@FXML
	private HBox songsHBox;
	@FXML
	private HBox playingHBox;
	
	private SubView subViewController;

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
		
		// time slider listener
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
		
		// search box listener
		searchBox.textProperty().addListener((observable, oldText, newText) -> {
			
			String text = newText.trim();
			
			if (text.equals("")) {
				// hide search popup when there is nothing in search box
				if (searchPopup.isShowing() && !searchHideAnimation.getStatus().equals(Animation.Status.RUNNING)) {
					searchHideAnimation.play();
				}
			} else {
				// otherwise, execute searching
				Search.search(text);
			}
		});
		
		// when searching process found results, show it
		Search.hasResultsProperty().addListener((observable, oldValue, newValue) -> {
			
			if (newValue) {
				
				// show results
				SearchResult result = Search.getResult();
				Platform.runLater(() -> {
					showSearchResults(result);
//					MusicPlayer.getStage().toFront();
				});
				
				// set height for search result popup
				int height = 0;
                int artists = result.getArtistResults().size();
                int albums = result.getAlbumResults().size();
                int songs = result.getSongResults().size();
                if (artists > 0) height += (artists * 50) + 50;
                if (albums > 0) height += (albums * 50) + 50;
                if (songs > 0) height += (songs * 50) + 50;
                if (height == 0) height = 50;
                searchPopup.setHeight(height);
			}
		});
		
		// hide search result popup when user moves or resizes the window
		MusicPlayer.getStage().xProperty().addListener((observable, oldValue, newValue) -> {
			if (searchPopup.isShowing() && !searchHideAnimation.getStatus().equals(Animation.Status.RUNNING)) {
				searchHideAnimation.play();
			}
		});
		MusicPlayer.getStage().yProperty().addListener((observable, oldValue, newValue) -> {
			if (searchPopup.isShowing() && !searchHideAnimation.getStatus().equals(Animation.Status.RUNNING)) {
				searchHideAnimation.play();
			}
		});
		
		// displays now playing song's info
		updateNowPlayingButton();
		initializeTimeLabels();
		initializeTimeSlider();
		//playlist
		initializePlaylists();
		
		// loads default sub view
		loadView("Songs");
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
	private void reimportMusic() {
		MusicPlayer.createLibraryXML();
		MusicPlayer.getNowPlayingList().clear();
		Thread thread = new Thread(MusicPlayer::prepareAndShowMain);
		thread.start();
	}
	
	@FXML
	private void selectView(Event e) {
		HBox eventSource = ((HBox)e.getSource());

		eventSource.requestFocus();

		Optional<Node> previous = sideBar.getChildren().stream()
			.filter(x -> x.getStyleClass().get(0).equals("sideBarItemSelected")).findFirst();

		if (previous.isPresent()) {
			HBox previousItem = (HBox) previous.get();
			previousItem.getStyleClass().setAll("sideBarItem");
		} else {
			previous = playlistBox.getChildren().stream()
					.filter(x -> x.getStyleClass().get(0).equals("sideBarItemSelected")).findFirst();
			if (previous.isPresent()) {
				HBox previousItem = (HBox) previous.get();
				previousItem.getStyleClass().setAll("sideBarItem");
			}
		}

		ObservableList<String> styles = eventSource.getStyleClass();
		String viewName = eventSource.getId();
		viewName = viewName.substring(0, 1).toUpperCase() + viewName.substring(1);
		setStyleAndLoad(viewName, styles);
	}
	
	public void setStyleAndLoad(String viewName, ObservableList<String> styles) {
		if (styles.get(0).equals("sideBarItem")) {
			styles.setAll("sideBarItemSelected");
			loadView(viewName);
		} else if (styles.get(0).equals("bottomBarItem")) {
			loadView(viewName);
		}
	}
	
	public SubView loadView(String viewName) {
		try {
			String fileName = Resources.FXML + viewName + ".fxml";
			
			System.out.println("Loading view " + fileName);
			FXMLLoader loader = new FXMLLoader(this.getClass().getResource(fileName));
			Node view = loader.load();
            
			CountDownLatch latch = new CountDownLatch(1);
            
			Task<Void> task = new Task<Void>() {
				protected Void call() throws Exception {
					Platform.runLater(() -> {
						Library.getSongs().stream().filter(x -> x.getSelected()).forEach(x -> x.setSelected(false));
						subViewRoot.setVisible(false);
						subViewRoot.setContent(view);
						subViewRoot.getContent().setOpacity(0);
						latch.countDown();
					});
					return null;
				}
			};

			task.setOnSucceeded(x -> new Thread(() -> {
				try {
					latch.await();
				} catch (Exception e) {
					e.printStackTrace();
				}
				Platform.runLater(() -> {
					subViewRoot.setVisible(true);
					loadViewAnimation.play();
				});
			}).start());

			Thread thread = new Thread(task);

			unloadViewAnimation.setOnFinished(x -> thread.start());

			loadViewAnimation.setOnFinished(x -> viewLoadedLatch.countDown());

			if (subViewRoot.getContent() == null) {
				
				subViewRoot.setContent(view);
				loadViewAnimation.play();
			} 
			else {
				unloadViewAnimation.play();
			}
			
			subViewController = loader.getController();
			return subViewController;
			
//			return loader.getController();
		}catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
	
	private Animation loadViewAnimation = new Transition() {
        {
            setCycleDuration(Duration.millis(250));
            setInterpolator(Interpolator.EASE_BOTH);
        }
        protected void interpolate(double frac) {
            subViewRoot.setVvalue(0);
            double curHeight = collapsedHeight + (expandedHeight - collapsedHeight) * (frac);
            subViewRoot.getContent().setTranslateY(expandedHeight - curHeight);
            subViewRoot.getContent().setOpacity(frac);
        }
    };
    
    private Animation unloadViewAnimation = new Transition() {
        {
            setCycleDuration(Duration.millis(250));
            setInterpolator(Interpolator.EASE_BOTH);
        }
        protected void interpolate(double frac) {
            double curHeight = collapsedHeight + (expandedHeight - collapsedHeight) * (1 - frac);
            subViewRoot.getContent().setTranslateY(expandedHeight - curHeight);
            subViewRoot.getContent().setOpacity(1 - frac);
        }
    };

	public SubView getSubViewController() {
		return subViewController;
	}
	
	private String checkDuplicatePlaylist(String text, int i) {
    	for (Playlist playlist : Library.getPlaylists()) {
    		if (playlist.getTitle().equals(text)) {
    			
    			int index = text.lastIndexOf(' ') + 1;
    			if (index != 0) {
    				try {
    					i = Integer.parseInt(text.substring(index));
    				} catch (Exception ex) {
    					// do nothing
    				}
    			}
    			
    			i++;
    			
    			if (i == 1) {
    				text = checkDuplicatePlaylist(text + " " + i, i);
    			} else {
    				text = checkDuplicatePlaylist(text.substring(0, index) + i, i);
    			}
    			break;
    		}
    	}
    	
    	return text;
    }
	
	private void initializePlaylists() {
    	for (Playlist playlist : Library.getPlaylists()) {
			if(playlist.getTitle().equals("Default")) continue;
    		try {
    			FXMLLoader loader = new FXMLLoader(this.getClass().getResource(Resources.FXML + "PlaylistCell.fxml"));
				HBox cell = loader.load();
				Label label = (Label) cell.getChildren().get(1);
				label.setText(playlist.getTitle());
				
				cell.setOnMouseClicked(x -> {
					selectView(x);
					((PlaylistsController) subViewController).selectPlaylist(playlist);
				});
				
				
//				PseudoClass hover = PseudoClass.getPseudoClass("hover");
				
				playlistBox.getChildren().add(cell);
				
			} catch (Exception e) {
				
				e.printStackTrace();
			}
    	}
    }

	@FXML
    private void newPlaylist() {
    	System.out.println("Create new playlist");
		if (!newPlaylistAnimation.getStatus().equals(Status.RUNNING)) 
		try {

			FXMLLoader loader = new FXMLLoader(this.getClass().getResource(Resources.FXML + "PlaylistCell.fxml"));
			HBox cell = loader.load();

			Label label = (Label) cell.getChildren().get(1);
			label.setVisible(false);
			HBox.setMargin(label, new Insets(0, 0, 0, 0));

			TextField textBox = new TextField();
			textBox.setPrefHeight(30);
			cell.getChildren().add(textBox);
			HBox.setMargin(textBox, new Insets(10, 10, 10, 9));

			textBox.focusedProperty().addListener((obs, oldValue, newValue) -> {
				if (oldValue && !newValue) {
					String text = textBox.getText().equals("") ? "New Playlist" : textBox.getText();
					text = checkDuplicatePlaylist(text, 0);
					label.setText(text);
					cell.getChildren().remove(textBox);
					HBox.setMargin(label, new Insets(10, 10, 10, 10));
					label.setVisible(true);
					Library.addPlaylist(text);
				}
			});

			textBox.setOnKeyPressed(x -> {
				if (x.getCode() == KeyCode.ENTER)  {
					sideBar.requestFocus();
				}
			});

			cell.setOnMouseClicked(x -> {
				selectView(x);
				Playlist playlist = Library.getPlaylist(label.getText());
				((PlaylistsController) subViewController).selectPlaylist(playlist);
			});
			

			cell.setPrefHeight(0);
			cell.setOpacity(0);

			playlistBox.getChildren().add(1, cell);

			textBox.requestFocus();

		} catch (Exception e) {

			e.printStackTrace();
		}
		
		newPlaylistAnimation.play();
        	
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
	 * Show search results
	 * @param result 
	 */
	public void showSearchResults(SearchResult result) {
		
		VBox root = (VBox) searchPopup.getScene().getRoot();
		ObservableList<Node> resultsList = root.getChildren();
		
		// clear old results
		resultsList.clear();
		
		// add new results
		// artist
		if (result.getArtistResults().size() > 0) {
			
			Label header = new Label("Artists");
			resultsList.add(header);
			VBox.setMargin(header, new Insets(10, 10, 10, 10));
			
			result.getArtistResults().forEach(artist -> {
				
				// artist cell
				HBox cell = new HBox();
                cell.setAlignment(Pos.CENTER_LEFT);
                cell.setPrefWidth(226);
                cell.setPrefHeight(50);
				
				// artist image
                ImageView image = new ImageView();
                image.setFitHeight(40);
                image.setFitWidth(40);
                //image.setImage(artist.getArtistImage());
				
				// artist title
                Label label = new Label(artist.getTitle());
                label.setTextOverrun(OverrunStyle.CLIP);
                label.getStyleClass().setAll("searchLabel");
				
				// add to artist cell
                cell.getChildren().addAll(image, label);
				
				// cell styles
				HBox.setMargin(image, new Insets(5, 5, 5, 5));
                HBox.setMargin(label, new Insets(10, 10, 10, 5));
                cell.getStyleClass().add("searchResult");
				
				// cell clicked handler
				cell.setOnMouseClicked(event -> {
					
				});
				
				resultsList.add(cell);
			});
			
			// separator line
			Separator separator = new Separator();
            separator.setPrefWidth(206);
            resultsList.add(separator);
            VBox.setMargin(separator, new Insets(10, 10, 0, 10));
		}
		// album
		if (result.getAlbumResults().size() > 0) {
			
			Label header = new Label("Albums");
			resultsList.add(header);
			VBox.setMargin(header, new Insets(10, 10, 10, 10));
			
			result.getAlbumResults().forEach(album -> {
				
				// album cell
				HBox cell = new HBox();
                cell.setAlignment(Pos.CENTER_LEFT);
                cell.setPrefWidth(226);
                cell.setPrefHeight(50);
				
				// album image
                ImageView image = new ImageView();
                image.setFitHeight(40);
                image.setFitWidth(40);
                //image.setImage(artist.getArtistImage());
				
				// album title
                Label label = new Label(album.getTitle());
                label.setTextOverrun(OverrunStyle.CLIP);
                label.getStyleClass().setAll("searchLabel");
				
				// add to album cell
                cell.getChildren().addAll(image, label);
				
				// cell styles
				HBox.setMargin(image, new Insets(5, 5, 5, 5));
                HBox.setMargin(label, new Insets(10, 10, 10, 5));
                cell.getStyleClass().add("searchResult");
				
				// cell clicked handler
				cell.setOnMouseClicked(event -> {
					
				});
				
				resultsList.add(cell);
			});
			
			// separator line
			Separator separator = new Separator();
            separator.setPrefWidth(206);
            resultsList.add(separator);
            VBox.setMargin(separator, new Insets(10, 10, 0, 10));
		}
		// song
		if (result.getSongResults().size() > 0) {
			
			Label header = new Label("Songs");
			resultsList.add(header);
			VBox.setMargin(header, new Insets(10, 10, 10, 10));
			
			result.getSongResults().forEach(song -> {
				
				// song cell
				HBox cell = new HBox();
                cell.setAlignment(Pos.CENTER_LEFT);
                cell.setPrefWidth(226);
                cell.setPrefHeight(50);
				
				// song title
                Label label = new Label(song.getTitle());
                label.setTextOverrun(OverrunStyle.CLIP);
                label.getStyleClass().setAll("searchLabel");
				
				// add to song cell
                cell.getChildren().add(label);
				
				// cell styles
                HBox.setMargin(label, new Insets(10, 10, 10, 5));
                cell.getStyleClass().add("searchResult");
				
				// cell clicked handler
				cell.setOnMouseClicked(event -> {
					
				});
				
				resultsList.add(cell);
			});
		}
		
		// if there is no result
		if (resultsList.isEmpty()) {
			Label label = new Label("No Result");
			resultsList.add(label);
			VBox.setMargin(label, new Insets(10, 10, 10, 10));
		}
		
		// show search result popup
		if (!searchPopup.isShowing()) {
            Stage stage = MusicPlayer.getStage();
            searchPopup.setX(stage.getX() + 18);
            searchPopup.setY(stage.getY() + 80);
            searchPopup.show();
            searchShowAnimation.play();
        }
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
			setCycleDuration(Duration.millis(50));
            setInterpolator(Interpolator.EASE_BOTH);
			setOnFinished(x -> MusicPlayer.getStage().toFront());
		}
		@Override
		protected void interpolate(double frac) {
			searchPopup.setOpacity(frac);
		}
	};
		
	private Animation searchHideAnimation = new Transition() {
		{
			setCycleDuration(Duration.millis(50));
            setInterpolator(Interpolator.EASE_BOTH);
			setOnFinished(x -> searchPopup.hide());
		}
		@Override
		protected void interpolate(double frac) {
			searchPopup.setOpacity(1.0 - frac);
		}
	};
	
	private Animation newPlaylistAnimation = new Transition() {
    	{
            setCycleDuration(Duration.millis(500));
            setInterpolator(Interpolator.EASE_BOTH);
        }
        protected void interpolate(double frac) {
    		HBox cell = (HBox) playlistBox.getChildren().get(1);
    		if (frac < 0.5) {
    			cell.setPrefHeight(frac * 100);
    		} else {
    			cell.setPrefHeight(50);
    			cell.setOpacity((frac - 0.5) * 2);
    		}
        }
    };

	public VBox getPlaylistBox() {
		return playlistBox;
	}

	public HBox getArtistsHBox() {
		return artistsHBox;
	}

	public HBox getAlbumsHBox() {
		return albumsHBox;
	}

	public HBox getSongsHBox() {
		return songsHBox;
	}

	public HBox getPlayingHBox() {
		return playingHBox;
	}
}
