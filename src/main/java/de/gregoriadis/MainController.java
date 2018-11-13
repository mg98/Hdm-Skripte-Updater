package de.gregoriadis;

import de.gregoriadis.Config.FileUpdateHandling;
import de.gregoriadis.scriptspage.Content;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.awt.MenuItem;

import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;

import javafx.scene.image.Image;

import java.awt.SystemTray;
import java.awt.Desktop;
import java.awt.TrayIcon;
import java.awt.PopupMenu;
import java.awt.Toolkit;
import java.awt.AWTException;
import java.io.File;
import java.io.IOException;

public class MainController {

    protected final static String filesRootDirectory = System.getProperty("user.home");

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
    private ScrollPane filesScrollPane;

    @FXML
    private HBox logoutHBox;

    @FXML
    private ToggleGroup fileUpdateHandling;

    @FXML
    private VBox syncWrapperVBox;

    @FXML
    private VBox settingsWrapperVBox;

    private Thread syncThread;

    private SystemTray tray;

    @FXML
    protected void initialize() {
        setupToolbar();

        directoryTextField.setText(Config.getInstance().getSyncDirectory());
        chooseDirectoryBtn.setOnMouseClicked(t -> {
            DirectoryChooser chooser = new DirectoryChooser();
            File defaultDirectory = new File(
                    Config.getInstance().getSyncDirectory());
            chooser.setInitialDirectory(defaultDirectory);
            File selectedDirectory = chooser.showDialog(Main.getPrimaryStage());
            directoryTextField.setText(selectedDirectory.getAbsolutePath());

            Config.getInstance().setSyncDirectory(selectedDirectory.getAbsolutePath());
            Config.getInstance().save();
        });

        syncBtn.setOnMouseClicked(t -> initSyncing());

        int radioIndex = Config.getInstance().getFileUpdateHandling().getRadioIndex();
        fileUpdateHandling.getToggles().get(radioIndex).setSelected(true);

        for (FileUpdateHandling fuh : FileUpdateHandling.values()) {
            fileUpdateHandling.getToggles().get(fuh.getRadioIndex()).setUserData(fuh);
        }

        fileUpdateHandling.selectedToggleProperty().addListener((ov, old_toggle, new_toggle) -> {
            if (fileUpdateHandling.getSelectedToggle() != null) {
                FileUpdateHandling setting = (FileUpdateHandling) fileUpdateHandling.getSelectedToggle().getUserData();
                Config.getInstance().setFileUpdateHandling(setting);
                Config.getInstance().save();
            }
        });

        logoutHBox.setOnMouseClicked(t -> Main.logout());
    }

    private void initSyncing() {
        settingsWrapperVBox.setVisible(false);
        syncWrapperVBox.setVisible(true);
        if (syncThread != null && syncThread.isAlive()) {
            Main.getLogger().info("Synchronization already in progress.");
        } else {
            filesScrollPane.setVisible(false);
            filesVBox.getChildren().clear();
            statusLabel.setText("Synchronisierung läuft...");

            java.awt.Image image = Toolkit.getDefaultToolkit().createImage("src/main/resources/img/progressTrayIcon.png");
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
                        HBox fileHBox = new HBox();

                        String imgPath = "/img/file.png";
                        if (content.isDownloaded()) imgPath = "/img/file-saved.png";
                        ImageView fileImageView = new ImageView(
                                new Image(Main.class.getResourceAsStream(imgPath)));
                        fileImageView.setFitWidth(18);
                        fileImageView.setFitHeight(18);

                        Label fileLabel = new Label(content.getLocalPath());
                        fileLabel.setStyle("-fx-padding: 0 0 6px 5px");

                        fileHBox.setStyle("-fx-cursor: hand");
                        fileHBox.setOnMouseClicked(t -> {
                            // Open file
                            try {
                                Desktop.getDesktop().open(content.getLocalFile());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });

                        fileHBox.getChildren().add(fileImageView);
                        fileHBox.getChildren().add(fileLabel);

                        Platform.runLater(() -> filesVBox.getChildren().add(fileHBox));
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
            java.awt.Image image = Toolkit.getDefaultToolkit().createImage("src/main/resources/img/favicon.png");
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
