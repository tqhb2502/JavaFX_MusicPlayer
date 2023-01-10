package model;

import java.io.File;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
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
}
