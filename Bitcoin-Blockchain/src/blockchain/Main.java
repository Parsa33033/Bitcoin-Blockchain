package blockchain;

import blockchain.controller.Blockchain;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Main extends Application{
	//field
	private double xOffset = 0;
    private double yOffset = 0;
	private static final int PORT = 40012;
	private static final int DPORT = 60019;
	private static final String IP = "224.2.2.2";
	
	private static Blockchain blockchain = new Blockchain(2);
	//methods
	public static void main(String[] args) throws Exception {
		launch();
		
	}
	
	@Override
	public void start(Stage stage) {
		try {
			Parent root = FXMLLoader.load(Main.class.getResource("\\view\\layout.fxml"));
			root.getStylesheets().add(Main.class.getResource("view\\style.css").toString());

	        root.setOnMousePressed(new EventHandler<MouseEvent>() {
	            @Override
	            public void handle(MouseEvent event) {
	                xOffset = event.getSceneX();
	                yOffset = event.getSceneY();
	            }
	        });
	        root.setOnMouseDragged(new EventHandler<MouseEvent>() {
	            @Override
	            public void handle(MouseEvent event) {
	                stage.setX(event.getScreenX() - xOffset);
	                stage.setY(event.getScreenY() - yOffset);
	            }
	        });
			Scene scene = new Scene(root);
			stage.setScene(scene);
			stage.initStyle(StageStyle.UNDECORATED);
			stage.show();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
}
