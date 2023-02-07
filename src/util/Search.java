/**
 * Quản lý kết quả tìm kiếm
 * Tạo luồng thực hiện việc tìm kiếm
 */
package util;

import java.util.List;
import java.util.stream.Collectors;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import model.Album;
import model.Artist;
import model.Library;
import model.SearchResult;
import model.Song;

public class Search {
	
	private static BooleanProperty hasResults = new SimpleBooleanProperty(false);
	private static SearchResult result;
	private static Thread searchThread;

	public static BooleanProperty hasResultsProperty() {
		return hasResults;
	}

	public static SearchResult getResult() {
		hasResults.set(false);
		return result;
	}
	
	public static void search(String searchText) {
		
		if (searchThread != null && searchThread.isAlive()) {
			searchThread.interrupt();
		}
		
		String text = searchText.toUpperCase();
		
		searchThread = new Thread(() -> {
			
			try {
				
				hasResults.set(false);
				
				// get list of songs contains the search text
				List<Song> songResults = Library.getSongs().stream()
					.filter(song -> song.getTitle().toUpperCase().contains(text))
					.sorted((x, y) -> {

						// exactly equals first
						boolean xMatch = x.getTitle().toUpperCase().equals(text);
						boolean yMatch = y.getTitle().toUpperCase().equals(text);
						if (xMatch && yMatch) return 0;
						if (xMatch) return -1;
						if (yMatch) return 1;

						// starts with the text first
						boolean xStartWith = x.getTitle().toUpperCase().startsWith(text);
						boolean yStartWith = y.getTitle().toUpperCase().startsWith(text);
						if (xStartWith && yStartWith) return 0;
						if (xStartWith) return -1;
						if (yStartWith) return 1;

						// contains a word starting with the text first
						boolean xContain = x.getTitle().toUpperCase().contains(" " + text);
						boolean yContain = y.getTitle().toUpperCase().contains(" " + text);
						if (xContain && yContain) return 0;
						if (xContain) return -1;
						if (yContain) return 1;
						
						return 0;
					})
					.collect(Collectors.toList());
				
				if (searchThread.isInterrupted()) {
					throw new InterruptedException();
				}
				
				// get list of albums contains the search text
				List<Album> albumResults = Library.getAlbums().stream()
					.filter(album -> album.getTitle().toUpperCase().contains(text))
					.sorted((x, y) -> {

						// exactly equals first
						boolean xMatch = x.getTitle().toUpperCase().equals(text);
						boolean yMatch = y.getTitle().toUpperCase().equals(text);
						if (xMatch && yMatch) return 0;
						if (xMatch) return -1;
						if (yMatch) return 1;

						// starts with the text first
						boolean xStartWith = x.getTitle().toUpperCase().startsWith(text);
						boolean yStartWith = y.getTitle().toUpperCase().startsWith(text);
						if (xStartWith && yStartWith) return 0;
						if (xStartWith) return -1;
						if (yStartWith) return 1;

						// contains a word starting with the text first
						boolean xContain = x.getTitle().toUpperCase().contains(" " + text);
						boolean yContain = y.getTitle().toUpperCase().contains(" " + text);
						if (xContain && yContain) return 0;
						if (xContain) return -1;
						if (yContain) return 1;
						
						return 0;
					})
					.collect(Collectors.toList());
				
				if (searchThread.isInterrupted()) {
					throw new InterruptedException();
				}
				
				// get list of artists contains the search text
				List<Artist> artistResults = Library.getArtists().stream()
					.filter(artist -> artist.getTitle().toUpperCase().contains(text))
					.sorted((x, y) -> {

						// exactly equals first
						boolean xMatch = x.getTitle().toUpperCase().equals(text);
						boolean yMatch = y.getTitle().toUpperCase().equals(text);
						if (xMatch && yMatch) return 0;
						if (xMatch) return -1;
						if (yMatch) return 1;

						// starts with the text first
						boolean xStartWith = x.getTitle().toUpperCase().startsWith(text);
						boolean yStartWith = y.getTitle().toUpperCase().startsWith(text);
						if (xStartWith && yStartWith) return 0;
						if (xStartWith) return -1;
						if (yStartWith) return 1;

						// contains a word starting with the text first
						boolean xContain = x.getTitle().toUpperCase().contains(" " + text);
						boolean yContain = y.getTitle().toUpperCase().contains(" " + text);
						if (xContain && yContain) return 0;
						if (xContain) return -1;
						if (yContain) return 1;
						
						return 0;
					})
					.collect(Collectors.toList());
				
				if (searchThread.isInterrupted()) {
					throw new InterruptedException();
				}
				
				// get only 3 first results (we will get 0th, 1st, 2nd in these lists)
				if (songResults.size() > 3) {
					songResults = songResults.subList(0, 3);
				}
				if (albumResults.size() > 3) {
					albumResults = albumResults.subList(0, 3);
				}
				if (artistResults.size() > 3) {
					artistResults = artistResults.subList(0, 3);
				}
				
				// pass to result attr
				result = new SearchResult(songResults, albumResults, artistResults);
				
				hasResults.set(true);
			} catch (InterruptedException e) {
				
			}
		});
		
		searchThread.start();
	}
}
