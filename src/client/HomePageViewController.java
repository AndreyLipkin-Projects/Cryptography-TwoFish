package client;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ResourceBundle;

import common.Utils;
import javafx.application.Preloader;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.stage.Window;

public class HomePageViewController extends Preloader implements Initializable {
	@FXML
	private TextField filePathTextField;
	@FXML
	private Label storingOutput;
	@FXML
	private ProgressIndicator storingSpinner;
	@FXML
	private TextField directoryTextField;
	@FXML
	private TextField fileNameTextField;
	@FXML
	private Label requestingOutput;
	@FXML
	private ProgressIndicator requestSpinner;
	@FXML
	private Button storeFileBtn;
	@FXML
	private Button getFileBtn;
	@FXML
	private TextField requestingKeyTF;
	@FXML
	private TextField storingKeyTF;
	@FXML
	private Button requestExchangeBtn;
	@FXML
	private Button storeExchangeBtn;

	Task copyWorker;
	Task copyWorker1;
	private File selectedFile;
	private VBox root;
	private Scene scene;
	private static FXMLLoader loader = null;
	private String userID;
	private Client client;
	private boolean fileChosen;
	private boolean storeKeyExchanged;

	private boolean dirChosen;
	private boolean requestKeyExchanged;

	public void start(Stage primaryStage) throws Exception {
		HomePageViewController controller = this;
		primaryStage.setScene(controller.loadMainScene());
		primaryStage.show();
	}

	public static HomePageViewController loadController() throws IOException {
		VBox root = null;
		Scene scene = null;
		if (loader == null) {
			loader = new FXMLLoader();
			loader.setLocation(HomePageViewController.class.getResource("HomePage.fxml"));
			root = (VBox) loader.load();
			scene = new Scene(root, root.getMinWidth(), root.getMinHeight());
		}
		HomePageViewController controller = loader.getController();
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
		this.client = new Client();
		addTextLimiter(storingKeyTF, 16);
		addTextLimiter(requestingKeyTF, 16);

		this.fileChosen = false;
		this.storeKeyExchanged = false;

		this.dirChosen = false;
		this.requestKeyExchanged = false;
	}

	public void setUserProps(String userId) {
		this.userID = userId;
	}

	@FXML
	void chooseFile(ActionEvent event) {
		// TODO Auto-generated method stub
//		this.storingSpinner.setProgress(0.1);
		storingSpinner.progressProperty().unbind();
		storingSpinner.setProgress(0.0);
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Open Resource File");
		fileChooser.getExtensionFilters().addAll(new ExtensionFilter("Audio Files", "*.wav"),
				new ExtensionFilter("All Files", "*.*"), new ExtensionFilter("Audio", "*.mp3"));
		Node source = (Node) event.getSource();
		Window theStage = source.getScene().getWindow();
		selectedFile = fileChooser.showOpenDialog(theStage);
		if (selectedFile != null) {
			this.filePathTextField.setText(selectedFile.getAbsolutePath());
//			this.storingSpinner.setProgress(1);
//			storeFileBtn.setDisable(false);
			this.fileChosen = true;
		}
		boolean val = fileChosen & storeKeyExchanged;
		storeFileBtn.setDisable(!val);
	}

	@FXML
	void onClickStoreFileBtn(ActionEvent event) {
		try {
			String wav = Utils.convertWAVtoHEX(this.selectedFile);
			// System.out.println(wav);
			System.out.println("length = " + wav.length());

			String[] output = Utils.Inputsplitter(wav, 32);
			// -------------------------------------------------------------------------------------------//
			// Fill the input file in the correct line length for further processing
			String name = selectedFile.getName().split("\\.")[0];
			File hexFile = new File(this.selectedFile.getParent() + "\\" + name + ".txt");
			FileWriter writer = new FileWriter(hexFile);
			int len = output.length;
			for (int i = 0; i < len; i++) {
				writer.write(output[i] + '\n');
				writer.flush();
			}
			writer.close();

			client.storeFile(hexFile, userID);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		storingSpinner.progressProperty().unbind();
		storingSpinner.setProgress(0.0);
		copyWorker = createWorker();
		storingSpinner.progressProperty().bind(copyWorker.progressProperty());
		copyWorker.messageProperty().addListener(new ChangeListener<String>() {
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
			}
		});
		new Thread(copyWorker).start();

	}

	@FXML
	void chooseDir(ActionEvent event) {
		Stage stage = (Stage) directoryTextField.getScene().getWindow();
		DirectoryChooser chooser = new DirectoryChooser();
		chooser.setTitle("Choose Download Destination");
		File selectedDirectory = chooser.showDialog(stage);
		if (selectedDirectory != null) {
			String dest = selectedDirectory.getAbsolutePath();
			directoryTextField.setText(dest);
			this.dirChosen = true;
		}
		// if user didn't choose, provide feedback and display error message
		if (directoryTextField.getText().isEmpty()) {
			this.dirChosen = false;
			directoryTextField.setStyle("-fx-text-fill: red; -fx-border-color: red;");
			directoryTextField.setText("Please provide download path");
		}
		boolean val = dirChosen & requestKeyExchanged;
		fileNameTextField.setDisable(!val);
		getFileBtn.setDisable(!val);
	}

	@FXML
	void onClickGetFile(ActionEvent event) {
		String fileName = fileNameTextField.getText();
		fileName += ".txt";
		if (fileName.isEmpty()) {
			requestingOutput.setTextFill(Color.RED);
			requestingOutput.setText("Please provide file name!");
			return;
		}
		File returnedFile = client.getFile(userID, fileName);
		if (returnedFile == null) {
			requestingOutput.setTextFill(Color.RED);
			requestingOutput.setText("server error");
			return;
		}
		Path temp = null;
		try {
			temp = Files.copy(Paths.get(returnedFile.getAbsolutePath()),
					Paths.get(directoryTextField.getText() + "\\" + fileName),StandardCopyOption.REPLACE_EXISTING);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		requestSpinner.progressProperty().unbind();
		requestSpinner.setProgress(0.0);
		copyWorker1 = createWorker();
		requestSpinner.progressProperty().bind(copyWorker1.progressProperty());
		copyWorker1.messageProperty().addListener(new ChangeListener<String>() {
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
			}
		});
		new Thread(copyWorker1).start();
		
		if (temp != null) {
			requestingOutput.setTextFill(Color.GREEN);
			requestingOutput.setText("downloaded successfully!");
//			System.out.println("File renamed and moved successfully");
		} else {
			requestingOutput.setTextFill(Color.RED);
			requestingOutput.setText("server error");
		}
	}

	@FXML
	void onClickExchange(ActionEvent event) {
		String id = ((Button) event.getSource()).getId();
		String key;
		if (id.equals("storeExchangeBtn")) {
			key = storingKeyTF.getText();
			if (key == null || key.equals("") || key.length() <= 3) {
				storingOutput.setTextFill(Color.RED);
				storingOutput.setText("Key must not be empty and at least 4 charcters long");
			} else {
				if (!client.exchangeKeys(key)) {
					storeKeyExchanged = false;
					storingOutput.setTextFill(Color.RED);
					storingOutput.setText("server error");
				} else {
					storeKeyExchanged = true;
					storingOutput.setTextFill(Color.GREEN);
					storingOutput.setText("Success!");

				}
			}
			boolean val = fileChosen & storeKeyExchanged;
			storeFileBtn.setDisable(!val);
		} else if (id.equals("requestExchangeBtn")) {
			key = requestingKeyTF.getText();
			if (key == null || key.equals("") || key.length() <= 3) {
				requestingOutput.setTextFill(Color.RED);
				requestingOutput.setText("Key must not be empty and at least 4 charcters long");
			} else {
				if (!client.exchangeKeys(key)) {
					requestingOutput.setTextFill(Color.RED);
					requestingOutput.setText("server error");
					this.requestKeyExchanged = false;
				} else {
					requestingOutput.setTextFill(Color.GREEN);
					requestingOutput.setText("Success!");
					this.requestKeyExchanged = true;

				}
			}
			boolean val = dirChosen & requestKeyExchanged;
			fileNameTextField.setDisable(!val);
			getFileBtn.setDisable(!val);
		}
	}

	public Task createWorker() {
		return new Task() {
			@Override
			protected Object call() throws Exception {
				for (int i = 0; i < 10; i++) {
					Thread.sleep(1000);
					updateMessage(String.valueOf(((i * 10) + 10)));
					updateProgress(i + 1, 10);
				}
				return true;
			}
		};
	}

	public static void addTextLimiter(final TextField tf, final int maxLength) {
		tf.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(final ObservableValue<? extends String> ov, final String oldValue,
					final String newValue) {
				if (tf.getText().length() > maxLength) {
					String s = tf.getText().substring(0, maxLength);
					tf.setText(s);
				}
			}
		});
	}

}
