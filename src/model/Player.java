package model;

import java.nio.file.Paths;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

public class Player {
        
    private Media media;
    private MediaPlayer mediaPlayer;
	
    public Player(String path) {
        media = new Media(Paths.get(path).toUri().toString());
        mediaPlayer = new MediaPlayer(media);
		mediaPlayer.setVolume(0.5);
    }

    public void play() {
        mediaPlayer.play();
    }

    public void pause(){
        mediaPlayer.pause();
    }

    public void restart() {
        mediaPlayer.seek(Duration.ZERO);
    }
	
    public void seek(int seconds) {
            mediaPlayer.seek(new Duration(seconds * 1000));
    }
}