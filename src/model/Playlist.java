package model;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javafx.collections.FXCollections;
import javafx.collections.ObservableArray;
import javafx.collections.ObservableList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.DOMException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import util.Resources;

public class Playlist {
	
    private int id;
    private String title;
    private ArrayList<Song> songs;

    public Playlist(int id, String title, ArrayList<Song> songs) {
        this.id = id;
        this.title = title;
        this.songs = songs;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public ObservableList<Song> getSongs() {
        return FXCollections.observableArrayList(songs);
    }
    
    public void addSong(Song s) {
		
        if(!songs.contains(s)) {
			
            songs.add(s);
            
            /// write to xml file code below
            try {
				
    			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
    			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
    			Document doc = docBuilder.parse(Resources.JAR + "library.xml");

    			XPathFactory xPathfactory = XPathFactory.newInstance();
    			XPath xpath = xPathfactory.newXPath();

    			XPathExpression expr = xpath.compile("/library/playlists/playlist[@id=\"" + this.id + "\"]");
    			Node playlist = ((NodeList) expr.evaluate(doc, XPathConstants.NODESET)).item(0);

    			Element songId = doc.createElement("songId");
    			songId.setTextContent(Integer.toString(s.getId()));
    			playlist.appendChild(songId);

    			TransformerFactory transformerFactory = TransformerFactory.newInstance();
    			Transformer transformer = transformerFactory.newTransformer();
    			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
    			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    			DOMSource source = new DOMSource(doc);
    			File xmlFile = new File(Resources.JAR + "library.xml");
    			StreamResult result = new StreamResult(xmlFile);
    			transformer.transform(source, result);

    		} catch (Exception ex) {
				ex.printStackTrace();
    		}
        }
    }
    
    public boolean removeSong(int songId) {
        for(Song s:this.songs) {
            if(s.getId() == songId) {
                this.songs.remove(s);
                return true;
                
            }
            
        }
        return false;
    }
    
	public String toString() {
		return this.title;
	}
}
