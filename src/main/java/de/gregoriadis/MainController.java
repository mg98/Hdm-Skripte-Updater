package de.gregoriadis;

import de.gregoriadis.scriptspage.Content;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.awt.MenuItem;

import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import org.controlsfx.control.Notifications;

import java.awt.SystemTray;
import java.awt.Desktop;
import java.awt.TrayIcon;
import java.awt.PopupMenu;
import java.awt.Toolkit;
import java.awt.Image;
import java.awt.AWTException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

    @FXML
    private ScrollPane filesScrollPane;

    private Thread syncThread;

    private SystemTray tray;

    @FXML
    protected void initialize() {
        setupToolbar();

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
            filesScrollPane.setVisible(false);
            filesVBox.getChildren().clear();
            statusLabel.setText("Synchronisierung läuft...");

            Image image = Toolkit.getDefaultToolkit().createImage("src/main/resources/img/progressTrayIcon.png");
            tray.getTrayIcons()[0].setImage(image);
            tray.getTrayIcons()[0].setToolTip("Synchronisiere...");


            Task<Void> syncTask = new Task<Void>() {
                Synchronizer sync = new Synchronizer();

                @Override
                protected Void call() throws Exception {
                    sync.sync();
                    return null;
                }

                @Override
                protected void succeeded() {
                    Platform.runLater(() -> {
                        setupToolbar();
                        statusLabel.setText("Fertig!");
                    });

                    if (sync.getLastAdded().size() > 0) filesScrollPane.setVisible(true);

                    for (Content content : sync.getLastAdded()) {
                        Label fileLabel = new Label(content.getLocalPath());
                        fileLabel.setStyle("-fx-cursor: pointer;");
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

                    INsUserNotificationsBridge.instance.sendNotification(
                            "Fertig", "",
                            "Synchronisierung abgeschlossen!", 0);
                }

                @Override
                protected void failed() {
                    super.failed();

                    Platform.runLater(() -> {
                        setupToolbar();
                        statusLabel.setText("Bei der Synchronisierung ist ein Fehler aufgetreten.");
                        //tray.remove(progressTray);
                    });

                    INsUserNotificationsBridge.instance.sendNotification(
                            "Fehler", "",
                            "Bei der Synchronisierung ist ein Fehler aufgetreten.", 0);
                }
            };
            syncThread = new Thread(syncTask);
            syncThread.start();
        }
    }

    private void setupToolbar() {
        if (SystemTray.isSupported()) {
            Image image = Toolkit.getDefaultToolkit().createImage("src/main/resources/img/favicon.png");
            PopupMenu popup = new PopupMenu();

            MenuItem syncMenuItem = new MenuItem("Synchronisieren");
            syncMenuItem.addActionListener(e -> initSyncing());

            popup.add(syncMenuItem);

            TrayIcon defaultTray = new TrayIcon(image, "HdM Skripte Updater", popup);

            if (tray != null && tray.getTrayIcons().length > 0) {
                TrayIcon trayIcon = tray.getTrayIcons()[0];
                trayIcon.setImage(defaultTray.getImage());
                trayIcon.setToolTip(defaultTray.getToolTip());
            } else {
                tray = SystemTray.getSystemTray();
                try {
                    tray.add(defaultTray);
                } catch (AWTException e) {
                    System.err.println(e);
                }
            }
        } else {
            Main.getLogger().warning("System Tray not supported!");
        }
    }

    protected void setStatus(String text) {
        Platform.runLater(() -> statusLabel.setText(text));
    }

}
