package main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MusicPlayer extends Application {

	@Override
	public void start(Stage stage) throws Exception {
            
            /// huy cmt
			// huy tiep tuc cmt
			// cmt thu 3 cua huy
                        // thai cmtaa
		
		Parent view = FXMLLoader.load(getClass().getResource("/resource/view/Main.fxml"));

		// Shows the scene containing the layout.
		Scene scene = new Scene(view);
		stage.setScene(scene);
		stage.setMaximized(true);
		stage.show();
	}
	
	public static void main(String[] args) {
		Application.launch(args);
	}
}
