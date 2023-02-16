package controller;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TableView.TableViewSelectionModel;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;

import main.MusicPlayer;
import model.Library;
import model.Song;
import util.SubView;
import util.PlayingTableCell;
import util.ControlPanelTableCell;
import util.ClippedTableCell;

public class SongsController implements Initializable, SubView {

	@FXML private TableView<Song> tableView;
    @FXML private TableColumn<Song, Boolean> playingColumn;
    @FXML private TableColumn<Song, String> titleColumn;
    @FXML private TableColumn<Song, String> artistColumn;
    @FXML private TableColumn<Song, String> albumColumn;
    @FXML private TableColumn<Song, String> lengthColumn;
    @FXML private TableColumn<Song, Integer> playsColumn;
    
	private String currentSortColumn = "titleColumn";
    private String currentSortOrder = null;
    
    private Song selectedSong;

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		titleColumn.prefWidthProperty().bind(tableView.widthProperty().subtract(50).multiply(0.26));
		artistColumn.prefWidthProperty().bind(tableView.widthProperty().subtract(50).multiply(0.26));
		albumColumn.prefWidthProperty().bind(tableView.widthProperty().subtract(50).multiply(0.26));
		lengthColumn.prefWidthProperty().bind(tableView.widthProperty().subtract(50).multiply(0.11));
		playsColumn.prefWidthProperty().bind(tableView.widthProperty().subtract(50).multiply(0.11));
		
		playingColumn.setCellFactory(x -> new PlayingTableCell<>());
		titleColumn.setCellFactory(x -> new ControlPanelTableCell<>());
		artistColumn.setCellFactory(x -> new ClippedTableCell<>());
		lengthColumn.setCellFactory(x -> new ClippedTableCell<>());
		playsColumn.setCellFactory(x -> new ClippedTableCell<>());
		
		playingColumn.setCellValueFactory(new PropertyValueFactory<>("playing"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        artistColumn.setCellValueFactory(new PropertyValueFactory<>("artist"));
        albumColumn.setCellValueFactory(new PropertyValueFactory<>("album"));
        lengthColumn.setCellValueFactory(new PropertyValueFactory<>("length"));
        playsColumn.setCellValueFactory(new PropertyValueFactory<>("playCount"));
		
		lengthColumn.setSortable(false);
		playsColumn.setSortable(false);
		
		tableView.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
			tableView.requestFocus();
			event.consume();
		});
		
//		ObservableList<Song> songs = Library.getPlaylist("Default").getSongs();
		ObservableList<Song> songs = Library.getSongs();
		
		Collections.sort(songs, (x, y) -> compareSongs(x, y));
		
		tableView.setItems(songs);
		
		tableView.setRowFactory(x -> {
			TableRow<Song> row = new TableRow<>();
			PseudoClass playing = PseudoClass.getPseudoClass("playing");
			
			ChangeListener<Boolean> changeListener = (obs, oldValue, newValue) -> 
					row.pseudoClassStateChanged(playing, newValue);
			
			row.itemProperty().addListener((obs, previousSong, currentSong) -> {
            	if (previousSong != null) {
            		previousSong.playingProperty().removeListener(changeListener);
            	}
            	if (currentSong != null) {
                    currentSong.playingProperty().addListener(changeListener);
                    row.pseudoClassStateChanged(playing, currentSong.getPlaying());
                } else {
                    row.pseudoClassStateChanged(playing, false);
                }
            });
			
			row.setOnMouseClicked(event -> {
				TableViewSelectionModel<Song> sm = tableView.getSelectionModel();
				if (event.getClickCount() == 2 && !row.isEmpty()) {    
//					selectedSong = tableView.getSelectionModel().getSelectedItems();
					selectedSong = sm.getSelectedItem();
//					System.out.println(selectedSong.getTitle());
					play();
				} else if (event.isShiftDown()) {
					ArrayList<Integer> indices = new ArrayList<>(sm.getSelectedIndices());
					if (indices.size() < 1) {
						if (indices.contains(row.getIndex())) {
							sm.clearSelection(row.getIndex());
						} else {
							sm.select(row.getItem());
						}
					} else {
						sm.clearSelection();
						indices.sort((first, second) -> first.compareTo(second));
						int max = indices.get(indices.size() - 1);
						int min = indices.get(0);
						if (min < row.getIndex()) {
							for (int i = min; i <= row.getIndex(); i++) {
								sm.select(i);
							}
						} else {
							for (int i = row.getIndex(); i <= max; i++) {
								sm.select(i);
							}
						}
					}

				} else if (event.isControlDown()) {
					if (sm.getSelectedIndices().contains(row.getIndex())) {
						sm.clearSelection(row.getIndex());
					} else {
						sm.select(row.getItem());
					}
				} else {
					if (sm.getSelectedIndices().size() > 1) {
						sm.clearSelection();
						sm.select(row.getItem());
					} else if (sm.getSelectedIndices().contains(row.getIndex())) {
						sm.clearSelection();
					} else {
						sm.clearSelection();
						sm.select(row.getItem());
					}
				}
			});
			
			return row;
			
		});
		
		tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
			if(oldSelection != null) {
				oldSelection.setSelected(false);
			}
			if(newSelection != null && tableView.getSelectionModel().getSelectedIndices().size() == 1) {
				newSelection.setSelected(true);
				selectedSong = newSelection;
			}
		});
		
		// Plays selected song when enter key is pressed.
		tableView.setOnKeyPressed(event -> {
			if (event.getCode().equals(KeyCode.ENTER)) {
				play();
			}
		});

		titleColumn.setComparator((x, y) -> {

			if (x == null && y == null) {
				return 0;
			} else if (x == null) {
				return 1;
			} else if (y == null) {
				return -1;
			}

			Song first = Library.getSong(x);
			Song second = Library.getSong(y);

			return compareSongs(first, second);
		});

		artistColumn.setComparator((first, second) -> Library.getArtist(first).compareTo(Library.getArtist(second)));

		albumColumn.setComparator((first, second) -> Library.getAlbum(first).compareTo(Library.getAlbum(second)));
		
	}	
	
	private int compareSongs(Song x, Song y) {
		if (x == null && y == null) {
    		return 0;
    	} else if (x == null) {
    		return 1;
    	} else if (y == null) {
    		return -1;
    	}
    	if (x.getTitle() == null && y.getTitle() == null) {
    		// Both are equal.
    		return 0;
    	} else if (x.getTitle() == null) {
    		// Null is after other strings.
    		return 1;
		} else if (y.getTitle() == null) {
			// All other strings are before null.
			return -1;
		} else  /*(x.getTitle() != null && y.getTitle() != null)*/ {
			return x.getTitle().compareTo(y.getTitle());
		}
	
	}
	
	@Override
	public void play() {
		Song song = selectedSong;
//		System.out.println("In song, play: " + selectedSong.getTitle());
		ObservableList<Song> songList = Library.getSongs();
		if(MusicPlayer.isShuffleActive()) {
			Collections.shuffle(songList);
			songList.remove(song);
			songList.add(0, song);
		}
		MusicPlayer.setNowPlayingList(songList);
		MusicPlayer.setNowPlaying(song);
		MusicPlayer.play();
	}
	
	@Override
	public Song getSelectedSong() {
		return selectedSong;
	}
	
}
