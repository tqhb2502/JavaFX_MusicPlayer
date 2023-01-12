/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * @author Admin
 */
public class Song implements Comparable<Song>{
	
    private int id;
    private SimpleStringProperty title;
    private SimpleStringProperty artist;
    private SimpleStringProperty album;
	private SimpleStringProperty length;
    private long lengthInSeconds;
    private int trackNumber;
    private int discNumber;
	private SimpleIntegerProperty playCount;
    private LocalDateTime playDate;
    private String location;
	private SimpleBooleanProperty playing;
    private SimpleBooleanProperty selected;

    public Song(int id, String title, String artist, String album, Duration length, 
				int trackNumber, int discNumber, int playCount, LocalDateTime playDate, String location) {
        
        if (title == null) {
            Path path = Paths.get(location);
            String fileName = path.getFileName().toString();
            title = fileName.substring(0, fileName.lastIndexOf('.'));
        }

        if (album == null) {
            album = "Unknown Album";
        }

        if (artist == null) {
            artist = "Unknown Artist";
        }
        
        this.id = id;
        this.title = new SimpleStringProperty(title);
        this.album = new SimpleStringProperty(album);
        this.artist = new SimpleStringProperty(artist);
		long seconds = length.getSeconds() % 60;
        this.length = new SimpleStringProperty(length.toMinutes() + ":" + (seconds < 10 ? "0" + seconds : seconds));
        this.trackNumber = trackNumber;
        this.discNumber = discNumber;
		this.playCount = new SimpleIntegerProperty(playCount);
        this.playDate = playDate;
        this.location = location;
        this.playing = new SimpleBooleanProperty(false);
        this.selected = new SimpleBooleanProperty(false);
    }

    public int getId() {
        return id;
    }
    

    public SimpleStringProperty titleProperty() {
        return title;
    }
    
    
    public String getTitle() {
        return title.get();
    }

    public SimpleStringProperty albumProperty() {
        return album;
    }
    
    public String getAlbum() {
        return album.get();
    }

    public String getArtist() {
        return this.artist.get();
    }

    public SimpleStringProperty artistProperty() {
        return this.artist;
    }

    public String getLength() {
        return this.length.get();
    }
	
	public StringProperty lengthProperty() {
        return this.length;
    }
	
	public long getLengthInSeconds() {
        return this.lengthInSeconds;
    }

    public int getTrackNumber() {
        return trackNumber;
    }

    public int getDiscNumber() {
        return discNumber;
    }
	
	public int getPlayCount() {
        return this.playCount.get();
    }

    public IntegerProperty playCountProperty() {
        return this.playCount;
    }

    public LocalDateTime getPlayDate() {
        return this.playDate;
    }
	
	public boolean getPlaying() {
        return this.playing.get();
    }

    public void setPlaying(boolean playing) {
        this.playing.set(playing);
    }

    public BooleanProperty selectedProperty() {
        return this.selected;
    }

    public boolean getSelected() {
        return this.selected.get();
    }

    public void setSelected(boolean selected) {
        this.selected.set(selected);
    }

    public String getLocation() {
        return location;
    }
	
	public void played() {
		
	}
	
    @Override
    public int compareTo(Song other) throws NullPointerException{
        if(this.discNumber == other.discNumber) {
			return this.trackNumber - other.trackNumber;
        }
        else return this.discNumber - other.discNumber;
    }
}
