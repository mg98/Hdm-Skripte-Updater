package de.gregoriadis;

import de.gregoriadis.scriptspage.Content;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import org.controlsfx.control.Notifications;
import org.jsoup.nodes.Document;

import java.awt.*;
import java.io.File;
import java.io.IOException;

public class MainController {

    public final static String filesRootDirectory = System.getProperty("user.home");

    @FXML
    private TextField directoryTextField;

    @FXML
    private Button chooseDirectoryBtn;

    @FXML
    private Button syncBtn;

    @FXML
    private Label statusLabel;

    @FXML
    private VBox filesVBox;

    @FXML
    protected void initialize() {

        directoryTextField.setText(Config.getInstance().getDirectory());
        chooseDirectoryBtn.setOnMouseClicked(t -> {
            DirectoryChooser chooser = new DirectoryChooser();
            File defaultDirectory = new File(Config.getInstance().getDirectory());
            chooser.setInitialDirectory(defaultDirectory);
            File selectedDirectory = chooser.showDialog(Main.getPrimaryStage());
            directoryTextField.setText(selectedDirectory.getAbsolutePath());

            Config.getInstance().setDirectory(selectedDirectory.getAbsolutePath());
            Config.getInstance().save();
        });

        syncBtn.setOnMouseClicked(t -> {

            statusLabel.setText("Synchronisierung läuft...");
            filesVBox.getChildren().clear();

            Task<Void> task = new Task<Void>() {
                Synchronizer sync = new Synchronizer();

                @Override
                protected Void call() throws Exception {
                    sync.sync();
                    return null;
                }

                @Override
                protected void succeeded() {
                    statusLabel.setText("Fertig!");
                    for (Content content : sync.getLastAdded()) {
                        System.out.println(content.getName());

                        Label fileLabel = new Label(content.getLocalPath());
                        fileLabel.setOnMouseClicked(t -> {
                            // Open file
                            try {
                                Desktop.getDesktop().open(content.getLocalFile());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });

                        filesVBox.getChildren().add(fileLabel);
                    }

                    Notifications
                            .create()
                            .title("Fertig")
                            .text("Synchronisierung abgeschlossen!")
                            .showInformation();
                }

                @Override
                protected void failed() {
                    super.failed();
                    statusLabel.setText("Bei der Synchronisierung ist ein Fehler aufgetreten.");

                    if (!Main.getPrimaryStage().isShowing()){
                        Main.getPrimaryStage().show();
                    }
                    if (!Main.getPrimaryStage().isFocused()) {
                        Main.getPrimaryStage().requestFocus();
                    }

                    Notifications
                            .create()
                            .title("Fehler")
                            .text("Bei der Synchronisierung ist ein Fehler aufgetreten.")
                            .showError();
                }
            };
            new Thread(task).start();
        });
    }

    protected void setStatus(String text) {
        statusLabel.setText(text);
    }

}
