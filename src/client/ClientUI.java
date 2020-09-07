package client;

import javafx.application.Application;
import javafx.stage.Stage;

public class ClientUI  extends Application{

	public ClientUI() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		// TODO Auto-generated method stub
		LoginViewController homePage = LoginViewController.loadController();
		primaryStage.setResizable(false);
		homePage.start(primaryStage);
	}

}
