/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.util.ArrayList;

/**
 *
 * @author Admin
 */
public class Album implements Comparable<Album>{
    private int id;
    private String artist;
    private String title;
    private ArrayList<Song> songs;

    public Album(int id, String title, String artist, ArrayList<Song> songs) {
        this.id = id;
        this.artist = artist;
        this.title = title;
        this.songs = songs;
    }

    public int getId() {
        return id;
    }

    public String getArtist() {
        return artist;
    }

    public String getTitle() {
        return title;
    }

    public ArrayList<Song> getSongs() {
        return songs;
    }
    
    @Override
    public int compareTo(Album other) {
        return this.title.compareTo(other.title);
    }
    
}
