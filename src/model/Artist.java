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
public class Artist implements Comparable<Artist>{
    private String title;
    private ArrayList<Album> albums;

    public Artist(String title, ArrayList<Album> albums) {
        this.title = title;
        this.albums = albums;
    }

    public String getTitle() {
        return title;
    }

    public ArrayList<Album> getAlbums() {
        return albums;
    }
    
    
    
    @Override
    public int compareTo(Artist other) {
        return this.title.compareTo(other.title);
    }
    
    
    
}
