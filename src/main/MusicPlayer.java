package main;

import controller.ImportMusicDialogController;
import controller.MainController;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import model.Album;
import model.Artist;
import model.Library;
import model.Song;
import util.Resources;

public class MusicPlayer extends Application {
	
	private static Stage stage;
	private static MainController mainController;

	// timer for updating time label and time slider
	private static Timer timer;
	private static int timerCounter;
	
	// media player
	private static MediaPlayer mediaPlayer;
	private static ArrayList<Song> nowPlayingList;
	private static Song nowPlaying;
	private static int nowPlayingIndex;
	private static int secondsPlayed;
	private static boolean isLoopActive = false;
	private static boolean isShuffleActive = false;
	private static boolean isMuted = false;
	
	// stores the number of files in library.xml
	// use to compare with the number of files in music directory to determine if library.xml needs to be updated
	private static int xmlFileNum;
	
	// stores the last id assigned to a song
	// use to determine id for new added song
	private static int lastIdAssigned;
	
	public static void main(String[] args) {
		Application.launch(MusicPlayer.class);
	}

	@Override
	public void start(Stage stage) throws Exception {
		
		// prepare timer
		timer = new Timer();
		timerCounter = 0;
		secondsPlayed = 0;
		
		// prepare stage
		MusicPlayer.stage = stage;
		MusicPlayer.stage.setTitle("Music Player");
		Image musicPlayerIcon = new Image(this.getClass().getResource(Resources.IMG + "Icon.png").toString());
		MusicPlayer.stage.getIcons().add(musicPlayerIcon);
		MusicPlayer.stage.setOnCloseRequest(event -> {
			Platform.exit();
			System.exit(0);
		});
		
		// loading stage
		try {
			
			// loading view
			FXMLLoader loader = new FXMLLoader(this.getClass().getResource(Resources.FXML + "SplashScreen.fxml"));
			Parent view = loader.load();
			
			// sets loading scene to stage
			Scene scene = new Scene(view);
			stage.setScene(scene);
			stage.setMaximized(true);
			stage.show();
			
			// check if library.xml exists, creates the file if it does not
			checkLibraryXML();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		Thread thread = new Thread(() -> {
			
			// retrieves songs, albums, artists, playlists data and stores to Library
			Library.getSongs();
			Library.getAlbums();
			Library.getArtists();
			// get playlists
			
			// retrieves playing list
			nowPlayingList = Library.loadPlayingList();
			
			// if now playing list is empty, set it with the songs of first artist in artists list
			if (nowPlayingList.isEmpty()) {
				
//				Artist artist = Library.getArtists().get(0);
//				
//				for (Album album : artist.getAlbums()) {
//					nowPlayingList.addAll(album.getSongs());
//				}
//				
//				Collections.sort(nowPlayingList, (first, second) -> {
//					Album firstAlbum = Library.getAlbum(first.getAlbum());
//                    Album secondAlbum = Library.getAlbum(second.getAlbum());
//					if (firstAlbum.compareTo(secondAlbum) != 0) {
//						return firstAlbum.compareTo(secondAlbum);
//					} else {
//						return first.compareTo(second);
//					}
//				});

				nowPlayingList.addAll(Library.getSongs());
			}
			
			nowPlaying = nowPlayingList.get(0);
			nowPlayingIndex = 0;
			nowPlaying.setPlaying(true);
			
			timer = new Timer();
            timerCounter = 0;
            secondsPlayed = 0;
			
			String path = nowPlaying.getLocation();
			Media media = new Media(Paths.get(path).toUri().toString());
			mediaPlayer = new MediaPlayer(media);
			mediaPlayer.setVolume(0.5);
			mediaPlayer.setOnEndOfMedia(new SongSkipper());
			
			// download image
			
			// Calls the function to initialize the main layout.
            Platform.runLater(this::initMain);
		});
		
		thread.start();
	}
	
	private static void checkLibraryXML() {
		
		// Finds the jar file and the path of its parent folder.
        File musicPlayerJAR = null;
        try {
            musicPlayerJAR = new File(MusicPlayer.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        String jarFilePath = musicPlayerJAR.getParentFile().getPath();
		
		// assign jar file path to resources
		Resources.JAR = jarFilePath + "/";
		
		// Specifies library.xml file and its location
		File libraryXML = new File(Resources.JAR + "library.xml");
		
		// store music directory path on computer
		Path musicDirectory;
		
		if (libraryXML.exists()) {
			
			// get music directory path from xml file
			musicDirectory = xmlMusicDirPathFinder();
			
			try {
				
				// find out the number of music files in music directory and XML file
				int musicDirFileNum = musicDirFileNumFinder(musicDirectory.toFile(), 0);
				xmlFileNum = xmlMusicDirFileNumFinder();
				
				// update XML file according to music directory
				if (musicDirFileNum != xmlFileNum) {
					updateLibraryXML(musicDirectory);
				}
				
			} catch (NullPointerException npe) {
				// catch if music directory's name has changed
				
				createLibraryXML();
				xmlFileNum = xmlMusicDirFileNumFinder();
			}
		} else if (!libraryXML.exists()) {
			
			createLibraryXML();
			xmlFileNum = xmlMusicDirFileNumFinder();
		}
	}
	
	/**
	 * Find music directory path stored in XML file
	 * @return path of music directory
	 */
	private static Path xmlMusicDirPathFinder() {
		try {
			
			// create xml reader
			XMLInputFactory factory = XMLInputFactory.newInstance();
            factory.setProperty("javax.xml.stream.isCoalescing", true);
            FileInputStream is = new FileInputStream(new File(Resources.JAR + "library.xml"));
            XMLStreamReader reader = factory.createXMLStreamReader(is, "UTF-8");
			
			String element = null;
			String path = null;
			
			// iterate xml document to find "path" element
			while (reader.hasNext()) {
				
				reader.next();
				
				if (reader.isWhiteSpace()) {
					continue;
				} else if (reader.isStartElement()) {
					element = reader.getName().getLocalPart();
				} else if (reader.isCharacters() && element.equals("path")) {
					path = reader.getText();
					break;
				}
			}
			
			// close xml reader
			reader.close();
			
			return Paths.get(path);
			
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Determine the number of music files stored in library.xml
	 * @return the number of music files
	 */
	private static int xmlMusicDirFileNumFinder() {
		try {
			
			// create xml reader
			XMLInputFactory factory = XMLInputFactory.newInstance();
            factory.setProperty("javax.xml.stream.isCoalescing", true);
            FileInputStream is = new FileInputStream(new File(Resources.JAR + "library.xml"));
            XMLStreamReader reader = factory.createXMLStreamReader(is, "UTF-8");
			
			String element = null;
			String fileNum = null;
			
			// iterate xml document to find "path" element
			while (reader.hasNext()) {
				
				reader.next();
				
				if (reader.isWhiteSpace()) {
					continue;
				} else if (reader.isStartElement()) {
					element = reader.getName().getLocalPart();
				} else if (reader.isCharacters() && element.equals("fileNum")) {
					fileNum = reader.getText();
					break;
				}
			}
			
			// close xml reader
			reader.close();
			
			return Integer.parseInt(fileNum);
			
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}
	
	/**
	 * Determine the number of music files stored in actual music directory on computer
	 * @param musicDirectory music directory instance
	 * @param i current number of files
	 * @return the number of music files in directory
	 */
	private static int musicDirFileNumFinder(File musicDirectory, int i) {
		
		File[] files = musicDirectory.listFiles();
		
		for (File file: files) {
			
			if (file.isFile() && Library.isSupportedFileType(file.getName())) {
				i++;
			} else if (file.isDirectory()) {
				i = musicDirFileNumFinder(file, i);
			}
		}
		
		return i;
	}
	
	/**
	 * Update library.xml
	 * @param musicDirectory path of music directory
	 */
	private static void updateLibraryXML(Path musicDirectory) {
		
	}
	
	private static void createLibraryXML() {
		try {
			
			// load import view
			FXMLLoader loader = new FXMLLoader(MusicPlayer.class.getResource(Resources.FXML + "ImportMusicDialog.fxml"));
			Parent view = loader.load();
			
			// create dialog stage
			Stage dialogStage = new Stage();
			dialogStage.setTitle("Music Player Configuration");
			// Forces user to focus on dialog.
            dialogStage.initModality(Modality.WINDOW_MODAL);
            // Sets minimal decorations for dialog.
            dialogStage.initStyle(StageStyle.UTILITY);
			// Prevents the alert from being re-sizable.
            dialogStage.setResizable(false);
            dialogStage.initOwner(stage);
			
			// set scene in dialog stage
			dialogStage.setScene(new Scene(view));
			
			// set dialog stage to import music dialog controller
			ImportMusicDialogController controller = loader.getController();
			controller.setDialogStage(dialogStage);
			
			// Show the dialog and wait until the user closes it.
            dialogStage.showAndWait();
			
			// Checks if the music was imported successfully. Closes the app otherwise.
			boolean importedSuccessfully = controller.isMusicImported();
			if (!importedSuccessfully) {
				System.exit(0);
			}
			
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	private void initMain() {
		try {
			
			// load main layout
			FXMLLoader loader = new FXMLLoader(this.getClass().getResource(Resources.FXML + "Main.fxml"));
			BorderPane view = loader.load();
			
			// show scene containing main layout
			double width = stage.getScene().getWidth();
            double height = stage.getScene().getHeight();
			
			view.setPrefWidth(width);
            view.setPrefHeight(height);
			
			Scene scene = new Scene(view);
            stage.setScene(scene);
			
			// Gives the controller access to the music player main application
			mainController = loader.getController();
			//mediaPlayer.volumeProperty().bind(mainController);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static Stage getStage() {
		return stage;
	}

	public static MainController getMainController() {
		return mainController;
	}
	
	public static void toggleLoop() {
		isLoopActive = !isLoopActive;
	}
	
	public static boolean isLoopActive() {
		return isLoopActive;
	}
	
	public static void toggleShuffle() {
		
		isShuffleActive = !isShuffleActive;
		
		if (isShuffleActive) {
			Collections.shuffle(nowPlayingList);
		} else {
			Collections.sort(nowPlayingList, (first, second) -> {
				
				Album firstAlbum = Library.getAlbum(first.getAlbum());
				Album secondAlbum = Library.getAlbum(second.getAlbum());
				
				int result = firstAlbum.compareTo(secondAlbum);
				if (result != 0) {
					return result;
				}
				
				result = first.compareTo(second);
				return result;
			});
		}
		
		nowPlayingIndex = nowPlayingList.indexOf(nowPlaying);
		
		// load subview
	}
	
	public static boolean isShuffleActive() {
		return isShuffleActive;
	}
	
	private static void updatePlayCount() {
		
	}
	
	public static Song getNowPlaying() {
		return nowPlaying;
	}
	
	public static void setNowPlaying(Song song) {
		
		if (nowPlayingList.contains(song)) {
			
			// updates prev song's info
			updatePlayCount();
			if (nowPlaying != null) {
				nowPlaying.setPlaying(false);
			}
			
			// assigns current song
			nowPlaying = song;
			nowPlaying.setPlaying(true);
			nowPlayingIndex = nowPlayingList.indexOf(song);
		
			// stop media player & timer
			if (mediaPlayer != null) {
				mediaPlayer.stop();
			}
			
			if (timer != null) {
				timer.cancel();
			}
			
			// set media player & timer for current xong
			timer = new Timer();
			timerCounter = 0;
			secondsPlayed = 0;
			
			String path = song.getLocation();
			Media media = new Media(Paths.get(path).toUri().toString());
			mediaPlayer = new MediaPlayer(media);
			// bind volume
			mediaPlayer.setOnEndOfMedia(new SongSkipper());
			mediaPlayer.setMute(isMuted);
			
			// set UI for current song
			mainController.updateNowPlayingButton();
			mainController.initializeTimeLabels();
			mainController.initializeTimeSlider();
		}
	}
	
	/**
	 * Get time passed in mm:ss
	 * @return mm:ss format string
	 */
	public static String getTimePassed() {
		int secondsPassed = timerCounter / 4;
		int minutes = secondsPassed / 60;
        int seconds = secondsPassed % 60;
        return Integer.toString(minutes) + ":" + (seconds < 10 ? "0" + seconds : Integer.toString(seconds));
	}
	
	/**
	 * Get time remaining in mm:ss
	 * @return mm:ss format string
	 */
	public static String getTimeRemaining() {
		long secondsPassed = timerCounter / 4;
		long totalSeconds = getNowPlaying().getLengthInSeconds();
		long secondsRemaining = totalSeconds - secondsPassed;
		long minutes = secondsRemaining / 60;
        long seconds = secondsRemaining % 60;
        return Long.toString(minutes) + ":" + (seconds < 10 ? "0" + seconds : Long.toString(seconds));
	}
	
	public static ArrayList<Song> getNowPlayingList() {
		return nowPlayingList == null ? new ArrayList<>() : new ArrayList<>(nowPlayingList);
    }

//	public static void setNowPlaying(Song nowPlaying) {
//		MusicPlayer.nowPlaying = nowPlaying;
//	}
	
	/**
	 * inner class for skipping song
	 */
	private static class SongSkipper implements Runnable {

		@Override
		public void run() {
			skip();
		}
	}
	
	/**
	 * inner class for updating time in UI
	 */
	private static class TimeUpdater extends TimerTask {
		
		private int length = (int) getNowPlaying().getLengthInSeconds() * 4;

		@Override
		public void run() {
			Platform.runLater(() -> {
				if (timerCounter < length) {
					timerCounter++;
					if (timerCounter % 4 == 0) {
						mainController.updateTimeLabels();
						secondsPlayed++;
					}
					// if slider is not pressed, vitually update
					if (!mainController.isTimeSliderPressed()) {
						mainController.updateTimeSlider();
					}
				}
			});
		}
	}
	
	/**
	 * Check if a song is being played
	 * @return true: playing, false: not playing
	 */
	public static boolean isPlaying() {
		return mediaPlayer != null && MediaPlayer.Status.PLAYING.equals(mediaPlayer.getStatus());
	}
	
	/**
	 * Play selected song
	 */
	public static void play() {
		if (mediaPlayer != null && !isPlaying()) {
			mediaPlayer.play();
			timer.scheduleAtFixedRate(new TimeUpdater(), 0, 250);
			mainController.updatePlayPauseIcon(true);
		}
	}
	
	/**
	 * pause selected song
	*/
	public static void pause() {
		if (isPlaying()) {
			mediaPlayer.pause();
			timer.cancel();
			timer = new Timer();
			mainController.updatePlayPauseIcon(false);
		}
	}
	
	/**
	 * seek
	 * @param seconds point of time
	 */
	public static void seek(int seconds) {
		if (mediaPlayer != null) {
			mediaPlayer.seek(new Duration(seconds * 1000));
			timerCounter = seconds * 4;
			mainController.updateTimeLabels();
		}
	}
	
	/**
	 * Skip song
	 */
	public static void skip() {
		
		boolean isPlaying = isPlaying();
		mainController.updatePlayPauseIcon(isPlaying);
		
		if (isLoopActive()) {
			setNowPlaying(nowPlayingList.get(nowPlayingIndex));
		} else {
			int nextSongIndex = (nowPlayingIndex + 1) % nowPlayingList.size();
			setNowPlaying(nowPlayingList.get(nextSongIndex));
		}
		
		if (isPlaying) {
			play();
		}
	}
	
	/**
	 * back to previous song
	 */
	public static void back() {
		
		boolean isPlaying = isPlaying();
		mainController.updatePlayPauseIcon(isPlaying);
		
		if (isLoopActive()) {
			setNowPlaying(nowPlayingList.get(nowPlayingIndex));
		} else {
			int nextSongIndex = (nowPlayingIndex + nowPlayingList.size() - 1) % nowPlayingList.size();
			setNowPlaying(nowPlayingList.get(nextSongIndex));
		}
		
		if (isPlaying) {
			play();
		}
	}
	
	/**
	 * mutes or unmutes media player
	 * @param isMuted true: mutes, false: unmutes
	 */
	public static void mute(boolean isMuted) {
        MusicPlayer.isMuted = isMuted;
        if (mediaPlayer != null) {
            mediaPlayer.setMute(isMuted);
        }
    }
}