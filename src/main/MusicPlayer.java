package main;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import util.Resources;

public class MusicPlayer extends Application {
	
	private static Stage stage;
	
	public static void main(String[] args) {
		Application.launch(MusicPlayer.class);
	}

	@Override
	public void start(Stage stage) throws Exception {
		
		// prepare stage
		MusicPlayer.stage = stage;
		MusicPlayer.stage.setTitle("Music Player");
		Image musicPlayerIcon = new Image(this.getClass().getResource(Resources.IMG + "Icon.png").toString());
		MusicPlayer.stage.getIcons().add(musicPlayerIcon);
		MusicPlayer.stage.setOnCloseRequest(event -> {
			Platform.exit();
			System.exit(0);
		});
		
		// load view
		FXMLLoader loader = new FXMLLoader(this.getClass().getResource(Resources.FXML + "Main.fxml"));
		Parent view = loader.load();

		// Shows the scene containing the layout.
		Scene scene = new Scene(view);
		stage.setScene(scene);
		stage.setMaximized(true);
		stage.show();
	}
	
	public static Stage getStage() {
		return stage;
	}
}
