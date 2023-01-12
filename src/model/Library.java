package model;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import main.MusicPlayer;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import util.ImportMusicTask;
import util.Resources;

public class Library {
	
	private static final String ID = "id";
    private static final String TITLE = "title";
    private static final String ARTIST = "artist";
    private static final String ALBUM = "album";
    private static final String LENGTH = "length";
    private static final String TRACKNUMBER = "trackNumber";
    private static final String DISCNUMBER = "discNumber";
    private static final String PLAYCOUNT = "playCount";
    private static final String PLAYDATE = "playDate";
    private static final String LOCATION = "location";
	
	private static ArrayList<Song> songs;
    private static ArrayList<Artist> artists;
    private static ArrayList<Album> albums;
    private static ArrayList<Playlist> playlists;
	private static int maxProgress;
	private static ImportMusicTask<Boolean> task;
	
	public static void importMusic(String path, ImportMusicTask<Boolean> task) throws Exception {
		
		// init library
		Library.maxProgress = 0;
		Library.task = task;
		
		// init XML document
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document doc = docBuilder.newDocument();
		
		// create main element
		Element library = doc.createElement("library");
        Element musicLibrary = doc.createElement("musicLibrary");
        Element songs = doc.createElement("songs");
        Element playlists = doc.createElement("playlists");
        Element nowPlayingList = doc.createElement("nowPlayingList");

		// append child to proper parent
		doc.appendChild(library);
        library.appendChild(musicLibrary);
        library.appendChild(songs);
        library.appendChild(playlists);
        library.appendChild(nowPlayingList);
		
		// more elements (music library path, number of files, and last song id assigned)
		Element musicLibraryPath = doc.createElement("path");
        Element musicLibraryFileNum = doc.createElement("fileNum");
        Element lastIdAssigned = doc.createElement("lastId");
		
		// add music library path to XML file
		musicLibraryPath.setTextContent(path);
		musicLibrary.appendChild(musicLibraryPath);
		
		// init song id
		int id = 0;
        File directory = new File(Paths.get(path).toUri());
		getMaxProgress(directory);
        Library.task.updateProgress(id, Library.maxProgress);
		
		// write songs' info to XML file
		int i = writeXML(directory, doc, songs, id);
		String fileNumber = Integer.toString(i);
		
		// add the number of music files to xml file
		musicLibraryFileNum.setTextContent(fileNumber);
        musicLibrary.appendChild(musicLibraryFileNum);
		
		// get last assigned song id and write to XML file
		int j = i - 1;
		lastIdAssigned.setTextContent(Integer.toString(j));
		musicLibrary.appendChild(lastIdAssigned);
		
		// transform source DOM to actual XML file
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");

        DOMSource source = new DOMSource(doc);

		File xmlFile = new File(Resources.JAR + "library.xml");
        StreamResult result = new StreamResult(xmlFile);
		
		transformer.transform(source, result);
		
		// reset
		Library.maxProgress = 0;
		Library.task = null;
	}
	
	/**
	 * Count the number of supported audio files
	 * @param directory importing directory
	 */
	private static void getMaxProgress(File directory) {
		
		File[] files = directory.listFiles(); // get all files in dir
		
		for (File file: files) {
			if (file.isFile() && isSupportedFileType(file.getName())) {
				Library.maxProgress++;
			} else if (file.isDirectory()) {
				getMaxProgress(file);
			}
		}
	}
	
	/**
	 * Write song's info to XML
	 * @param directory
	 * @param doc
	 * @param songs
	 * @param i
	 * @return Last ID
	 * @throws Exception 
	 */
	private static int writeXML(File directory, Document doc, Element songs, int i) throws Exception {
		
		File[] files = directory.listFiles();
		
		for (File file: files) {
			
			if (file.isFile() && isSupportedFileType(file.getName())) {
				
				try {
					
					// create audio file's tagger
					AudioFile audioFile = AudioFileIO.read(file);
					Tag tag = audioFile.getTag();
					AudioHeader header = audioFile.getAudioHeader();
					
					// create song element
					Element song = doc.createElement("song");
					songs.appendChild(song);
					
					// song element's childs (song's info)
					Element id = doc.createElement("id");
                    Element title = doc.createElement("title");
                    Element artist = doc.createElement("artist");
                    Element album = doc.createElement("album");
                    Element length = doc.createElement("length");
                    Element trackNumber = doc.createElement("trackNumber");
                    Element discNumber = doc.createElement("discNumber");
                    Element playCount = doc.createElement("playCount");
                    Element playDate = doc.createElement("playDate");
                    Element location = doc.createElement("location");
					
					// fill song's info into elements
					id.setTextContent(Integer.toString(i));
					title.setTextContent(tag.getFirst(FieldKey.TITLE));
					String artistTitle = tag.getFirst(FieldKey.ALBUM_ARTIST);
                    if (artistTitle == null || artistTitle.equals("") || artistTitle.equals("null")) {
                        artistTitle = tag.getFirst(FieldKey.ARTIST);
                    }
					artist.setTextContent(
						(artistTitle == null || artistTitle.equals("") || artistTitle.equals("null")) ? "" : artistTitle
					);
					album.setTextContent(tag.getFirst(FieldKey.ALBUM));
					length.setTextContent(Integer.toString(header.getTrackLength()));
					String track = tag.getFirst(FieldKey.TRACK);
                    trackNumber.setTextContent(
                            (track == null || track.equals("") || track.equals("null")) ? "0" : track
                    );
					String disc = tag.getFirst(FieldKey.DISC_NO);
                    discNumber.setTextContent(
                            (disc == null || disc.equals("") || disc.equals("null")) ? "0" : disc
                    );
					playCount.setTextContent("0");
                    playDate.setTextContent(LocalDateTime.now().toString());
                    location.setTextContent(Paths.get(file.getAbsolutePath()).toString());
					
					// append child elements to song element
					song.appendChild(id);
                    song.appendChild(title);
                    song.appendChild(artist);
                    song.appendChild(album);
                    song.appendChild(length);
                    song.appendChild(trackNumber);
                    song.appendChild(discNumber);
                    song.appendChild(playCount);
                    song.appendChild(playDate);
                    song.appendChild(location);
					
					// update importing progress
					task.updateProgress(i, maxProgress);
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if (file.isDirectory()) {
				i = writeXML(directory, doc, songs, i);
			}
		}
		
		return i;
	}
	
	/**
	 * Check if the file is supported
	 * @param fileName name of the file
	 * @return true: supported, false: not supported
	 */
	public static boolean isSupportedFileType(String fileName) {

        String extension = "";
        int i = fileName.lastIndexOf('.');
        if (i > 0) {
            extension = fileName.substring(i+1).toLowerCase();
        }
		
        switch (extension) {
            // MP3
            case "mp3":
            // MP4
            case "mp4":
            case "m4a":
            case "m4v":
            // WAV
            case "wav":
                return true;
            default:
                return false;
        }
    }
	
	/**
	 * Get songs list
	 * @return observable list of songs
	 */
	public static ObservableList<Song> getSongs() {
		
		if (songs == null) {
			songs = new ArrayList<>();
			updateSongsList();
		}
		
		return FXCollections.observableArrayList(songs);
	}
	
	public static Song getSong(int id) {
		
		if (songs == null) {
            getSongs();
        }
		
        return songs.get(id);
	}
	
	public static Song getSong(String title) {
		
		if (songs == null) {
			getSongs();
		}
		
		return songs.stream().filter(song -> title.equals(song.getTitle())).findFirst().get();
	}
	
	/**
	 * Read songs list from XML file
	 */
	private static void updateSongsList() {
		
		try {
			
			// prepare XML stream reader
			XMLInputFactory factory = XMLInputFactory.newInstance();
            factory.setProperty("javax.xml.stream.isCoalescing", true);
            FileInputStream is = new FileInputStream(new File(Resources.JAR + "library.xml"));
            XMLStreamReader reader = factory.createXMLStreamReader(is, "UTF-8");
			
			// prepare data holders
			String element = "";
            int id = -1;
            String title = null;
            String artist = null;
            String album = null;
            Duration length = null;
            int trackNumber = -1;
            int discNumber = -1;
            int playCount = -1;
            LocalDateTime playDate = null;
            String location = null;
			
			// iterate through XML document
			while (reader.hasNext()) {
			
				reader.next();
				
				if (reader.isWhiteSpace()) {
					continue;
				} else if (reader.isStartElement()) {
					// start tag
					element = reader.getName().getLocalPart();
				} else if (reader.isCharacters()) {
					// data
					String value = reader.getText();
					
					switch (element) {
                        case ID:
                            id = Integer.parseInt(value);
                            break;
                        case TITLE:
                            title = value;
                            break;
                        case ARTIST:
                            artist = value;
                            break;
                        case ALBUM:
                            album = value;
                            break;
                        case LENGTH:
                            length = Duration.ofSeconds(Long.parseLong(value));
                            break;
                        case TRACKNUMBER:
                            trackNumber = Integer.parseInt(value);
                            break;
                        case DISCNUMBER:
                            discNumber = Integer.parseInt(value);
                            break;
                        case PLAYCOUNT:
                            playCount = Integer.parseInt(value);
                            break;
                        case PLAYDATE:
                            playDate = LocalDateTime.parse(value);
                            break;
                        case LOCATION:
                            location = value;
                            break;
                    }
				} else if (reader.isEndElement() && reader.getName().getLocalPart().equals("song")) {
					// end tag of song
					
					// add song
					songs.add(new Song(id, title, artist, album, length, trackNumber, discNumber, playCount, playDate, location));
					
					// reset
					id = -1;
                    title = null;
                    artist = null;
                    album = null;
                    length = null;
                    trackNumber = -1;
                    discNumber = -1;
                    playCount = -1;
                    playDate = null;
                    location = null;
				} else if (reader.isEndElement() && reader.getName().getLocalPart().equals("songs")) {
					// end tag of song list
					reader.close();
					break;
				}
			}
			
			reader.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static ObservableList<Album> getAlbums() {
		
		if (albums == null) {
			if (songs == null) {
				getSongs();
			}
			updateAlbumsList();
		}
		
		return FXCollections.observableArrayList(albums);
	}
	
	public static Album getAlbum(String title) {
		
		if (albums == null) {
			getAlbums();
		}
		
		return albums
				.stream()
				.filter(album -> title.equals(album.getTitle()))
				.findFirst()
				.get();
	}
	
	/**
	 * Get albums list
	 */
	private static void updateAlbumsList() {
		
		albums = new ArrayList<>();
		
		// grouping by album's name
		TreeMap<String, List<Song>> albumMap = new TreeMap<>(
			songs
				.stream()
				.filter(song -> song.getAlbum() != null)
				.collect(Collectors.groupingBy(Song::getAlbum))
		);
		
		int id = 0;
		
		// grouping one more time by artist's name
		for (Map.Entry<String, List<Song>> entry : albumMap.entrySet()) {
			
			ArrayList<Song> currentSongsList = new ArrayList<>();
			currentSongsList.addAll(entry.getValue());
			
			TreeMap<String, List<Song>> artistMap = new TreeMap<>(
				currentSongsList
					.stream()
					.filter(song -> song.getArtist() != null)
					.collect(Collectors.groupingBy(Song::getArtist))
			);
			
			for (Map.Entry<String, List<Song>> e : artistMap.entrySet()) {
				
				ArrayList<Song> albumSongs = new ArrayList<>();
				String artist = e.getValue().get(0).getArtist();
				
				albumSongs.addAll(e.getValue());
				
				albums.add(new Album(id++, entry.getKey(), artist, albumSongs));
			}
		}
	}
	
	public static ObservableList<Artist> getArtists() {
		
		if (artists == null) {
			if (albums == null) {
				getAlbums();
			}
			updateArtistsList();
		}
		
		return FXCollections.observableArrayList(artists);
	}
	
	public static Artist getArtist(String title) {
		
		if (artists == null) {
			getArtists();
		}
		
		return artists
				.stream()
				.filter(artist -> title.equals(artist.getTitle()))
				.findFirst()
				.get();
	}
	
	/**
	 * Get artists list
	 */
	private static void updateArtistsList() {
		
		artists = new ArrayList<>();
		
		TreeMap<String, List<Album>> artistMap = new TreeMap<>(
			albums
				.stream()
				.filter(album -> album.getArtist() != null)
				.collect(Collectors.groupingBy(Album::getArtist))
		);
		
		for (Map.Entry<String, List<Album>> entry : artistMap.entrySet()) {
			
			ArrayList<Album> artistAlbums = new ArrayList<>();
			artistAlbums.addAll(entry.getValue());
			artists.add(new Artist(entry.getKey(), artistAlbums));
		}
	}
	
	// playlist
	
	/**
	 * Get playing list from library.xml
	 * @return playing list
	 */
	public static ArrayList<Song> loadPlayingList() {
		
		ArrayList<Song> nowPlayingList = new ArrayList<>();
		
		try {
			
			XMLInputFactory factory = XMLInputFactory.newInstance();
			FileInputStream is = new FileInputStream(new File(Resources.JAR + "library.xml"));
			XMLStreamReader reader = factory.createXMLStreamReader(is, "UTF-8");
			
			String element = "";
			boolean isNowPlayingList = false;
			
			while (reader.hasNext()) {
				
				reader.next();
				
				if (reader.isWhiteSpace()) {
                    continue;
                } else if (reader.isCharacters() && isNowPlayingList) {
					
					String value = reader.getText();
					if (element.equals(ID)) {
						nowPlayingList.add(getSong(Integer.parseInt(value)));
					}
				} else if (reader.isStartElement()) {
					
					element = reader.getName().getLocalPart();
					if (element.equals("nowPlayingList")) {
						isNowPlayingList = true;
					}
				} else if (reader.isEndElement() && reader.getName().getLocalPart().equals("nowPlayingList")) {
					
					reader.close();
					break;
				}
			}
			
			reader.close();
						
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
		
		return nowPlayingList;
	}
	
	public static void savePlayingList() {
		
		Thread thread = new Thread(() -> {
			
			try {
				
				// get nowPlayingList node in xml document
				DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
                Document doc = docBuilder.parse(Resources.JAR + "library.xml");
				
				XPathFactory xPathfactory = XPathFactory.newInstance();
                XPath xpath = xPathfactory.newXPath();
				
				XPathExpression expr = xpath.compile("/library/nowPlayingList");
                Node playingList = ((NodeList) expr.evaluate(doc, XPathConstants.NODESET)).item(0);
				
				// remove old playing list
				NodeList nodes = playingList.getChildNodes();
				while (nodes.getLength() > 0) {
					playingList.removeChild(nodes.item(0));
				}
				
				// add new playing list
				for (Song song : MusicPlayer.getNowPlayingList()) {
					Element id = doc.createElement(ID);
					id.setTextContent(Integer.toString(song.getId()));
					playingList.appendChild(id);
				}
				
				// write to library.xml
				TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();
                transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
				
				DOMSource source = new DOMSource(doc);
                File xmlFile = new File(Resources.JAR + "library.xml");
                StreamResult result = new StreamResult(xmlFile);
				
				transformer.transform(source, result);
				
			} catch (Exception e) {
				
				e.printStackTrace();
			}
		});
		
		thread.start();
	}
}
