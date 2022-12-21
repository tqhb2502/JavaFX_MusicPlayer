
package model;
import java.nio.file.Paths;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

public class Player {
        
    private Media media;
    private MediaPlayer media_player;
    public Player(String source) {
        media = new Media(Paths.get(source).toUri().toString());
        media_player = new MediaPlayer(media);

    }
    
//    public void setPath(String source) {
//        
//        media = new Media(Paths.get(source).toUri().toString());
//        media_player = new MediaPlayer(media);
//    }

    public void play() {

        media_player.play();
        
    }

    public void pause(){
        media_player.pause();
    }

    public void restart() {
        media_player.seek(Duration.ZERO);
    }
}