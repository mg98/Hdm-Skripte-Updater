package de.gregoriadis;

import de.gregoriadis.scriptspage.Content;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import org.controlsfx.control.Notifications;
import org.jsoup.nodes.Document;

import java.awt.Desktop;
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
    private Pane pane;

    private Thread syncThread;

    @FXML
    protected void initialize() {
        setupMenuBar();


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

        syncBtn.setOnMouseClicked(t -> initSyncing());
    }

    private void initSyncing() {
        if (syncThread != null && syncThread.isAlive()) {
            Main.getLogger().info("Synchronization already in progress.");
        } else {
            Task<Void> syncTask = new Task<Void>() {
                Synchronizer sync = new Synchronizer();

                @Override
                protected Void call() throws Exception {
                    Platform.runLater(() -> {
                        statusLabel.setText("Synchronisierung läuft...");
                        filesVBox.getChildren().clear();
                    });
                    sync.sync();
                    return null;
                }

                @Override
                protected void succeeded() {
                    setStatus("Fertig!");

                    for (Content content : sync.getLastAdded()) {
                        Label fileLabel = new Label(content.getLocalPath());
                        fileLabel.setOnMouseClicked(t -> {
                            // Open file
                            try {
                                Desktop.getDesktop().open(content.getLocalFile());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });

                        Platform.runLater(() -> filesVBox.getChildren().add(fileLabel));
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
                    setStatus("Bei der Synchronisierung ist ein Fehler aufgetreten.");

                    if (!Main.getPrimaryStage().isShowing()) {
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
            syncThread = new Thread(syncTask);
            syncThread.start();
        }
    }

    private void setupMenuBar() {
        MenuBar menuBar = new MenuBar();
        menuBar.useSystemMenuBarProperty().set(true);

        Menu menu = new Menu("HdMSkripte");
        MenuItem item = new MenuItem("Synchronisieren");
        item.setOnAction(e -> initSyncing());

        menu.getItems().add(item);
        menuBar.getMenus().add(menu);

        pane.getChildren().add(menuBar);
    }

    protected void setStatus(String text) {
        Platform.runLater(() -> statusLabel.setText(text));
    }

}
