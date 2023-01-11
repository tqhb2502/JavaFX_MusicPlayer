/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.nio.file.Path;
import java.nio.file.Paths;
import javafx.beans.property.SimpleStringProperty;
import javafx.util.Duration;

/**
 *
 * @author Admin
 */
public class Song implements Comparable<Song>{
    private int id;
    private SimpleStringProperty title;
    private SimpleStringProperty album;
    private SimpleStringProperty artist;
    private long length;
    private int trackNumber;
    private int discNumber;
    private String location;

    public Song(int id, String title, Duration length, int trackNumber, int discNumber, String location, String album, String artist) {
        
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
//        this.length = length.get;
        this.trackNumber = trackNumber;
        this.discNumber = discNumber;
        this.location = location;
        
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

    public long getLength() {
        return length;
    }

    public int getTrackNumber() {
        return trackNumber;
    }

    public int getDiscNumber() {
        return discNumber;
    }

    public String getLocation() {
        return location;
    }

    
       
    
    
    @Override
    public int compareTo(Song other) throws NullPointerException{
        if(this.trackNumber == other.trackNumber) {
            return this.discNumber - other.discNumber;
        }
        else return this.trackNumber - other.trackNumber;
    }
       
        
}
