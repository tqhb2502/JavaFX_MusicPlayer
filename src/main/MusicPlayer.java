package main;

import controller.MainController;
import java.nio.file.Paths;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import javafx.util.Duration;
import util.Resources;

public class MusicPlayer extends Application {
	
	private static Stage stage;
	
	private static MainController mainController;
	private static MediaPlayer mediaPlayer; // music media
	private static Timer timer;	// timer for scheduling task
	private static int timerCounter;
	private static int secondsPlayed;
	
	private static boolean isLoopActive = false;
	private static boolean isShuffleActive = false;
	private static boolean isMuted = false;
	
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
		
		// load view
		FXMLLoader loader = new FXMLLoader(this.getClass().getResource(Resources.FXML + "Main.fxml"));
		Parent view = loader.load();

		// Shows the scene containing the layout.
		Scene scene = new Scene(view);
		stage.setScene(scene);
		stage.setMaximized(true);
		stage.show();
		
		mainController = loader.getController();
		
		// play music
		String path = "src/resource/song/OldTownRoad.mp3";
		Media media = new Media(Paths.get(path).toUri().toString());
        mediaPlayer = new MediaPlayer(media);
		mediaPlayer.setVolume(0.5);
	}
	
	public static Stage getStage() {
		return stage;
	}
	
	public static void toggleLoop() {
		isLoopActive = !isLoopActive;
	}
	
	public static boolean isLoopActive() {
		return isLoopActive;
	}
	
	public static void toggleShuffle() {
		isShuffleActive = !isShuffleActive;
	}
	
	public static boolean isShuffleActive() {
		return isShuffleActive;
	}
	
	public static void toggleMute() {
		isMuted = !isMuted;
		if (mediaPlayer != null) {
			mediaPlayer.setMute(!isMuted);
		}
	}
	
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
		
		//private int length = (int) getNowPlaying().getLengthInSeconds() * 4;
		private int length = (int) 157 * 4;

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
	 * Check if a song is being played
	 * @return true: playing, false: not playing
	 */
	public static boolean isPlaying() {
		return mediaPlayer != null && MediaPlayer.Status.PLAYING.equals(mediaPlayer.getStatus());
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
		
	}
	
	/**
	 * back to previous song
	 */
	public static void back() {
		
	}
}
