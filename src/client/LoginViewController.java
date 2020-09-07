package client;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Preloader;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.stage.Window;

public class LoginViewController extends Preloader implements Initializable {

	@FXML
	private PasswordField passwordTextField;
	@FXML
	private TextField  userIDTextField;

	private VBox root;
	private Scene scene;
	private static FXMLLoader loader = null;
	@FXML
	private Label warningMsg;

	private Client client;

	public void start(Stage primaryStage) throws Exception {
		LoginViewController controller = LoginViewController.loadController();
		primaryStage.setScene(controller.loadMainScene());
		primaryStage.show();
	}

	public static LoginViewController loadController() throws IOException {
		VBox root = null;
		Scene scene = null;
		if (loader == null) {
			loader = new FXMLLoader();
			loader.setLocation(LoginViewController.class.getResource("LoginView.fxml"));
			root = (VBox) loader.load();
			scene = new Scene(root, root.getMinWidth(), root.getMinHeight());
		}
		LoginViewController controller = loader.getController();
		if (root != null) {
			controller.root = root;
		}
		if (scene != null) {
			controller.scene = scene;
		}
		return controller;
	}

	public Scene loadMainScene() throws IOException {
		return scene;
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		this.warningMsg.setVisible(false);
		this.client = new Client();
	}

	@FXML
	void onClickLoginBtn(ActionEvent event) {
		String userID = userIDTextField.getText();
		String password = passwordTextField.getText();
		if (client.login(userID, password)) {
			this.client.setUserID(userID);
			// the password is correct move to next window.
			Node source = (Node) event.getSource();
			Window theStage = source.getScene().getWindow();
			HomePageViewController homePage;
			try {
				homePage = HomePageViewController.loadController();
				homePage.setUserProps(userID);
				homePage.start((Stage) theStage);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			this.warningMsg.setText("Wrong userID or password!");
			this.warningMsg.setTextFill(Color.RED);
			this.warningMsg.setVisible(true);
		}
	}

	@FXML
	void passTxtFieldChanged(KeyEvent event) {
		this.warningMsg.setVisible(false);

	}
    @FXML
    void onClickRegisterBtn(ActionEvent event) {
		String userID = userIDTextField.getText();
		String password = passwordTextField.getText();
		if(userID.isEmpty() || password.isEmpty()) {
			this.warningMsg.setText("Username and/or password is empty");
			this.warningMsg.setTextFill(Color.RED);
			this.warningMsg.setVisible(true);
			return;
		}
		if(client.register(userID, password)){
			this.warningMsg.setText("Registered Successfully!");
			this.warningMsg.setTextFill(Color.GREEN);
			this.warningMsg.setVisible(true);
		}else {
			this.warningMsg.setText("User exists");
			this.warningMsg.setTextFill(Color.RED);
			this.warningMsg.setVisible(true);
		}
    }

}
