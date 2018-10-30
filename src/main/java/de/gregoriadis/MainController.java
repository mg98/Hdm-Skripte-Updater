package de.gregoriadis;

import de.gregoriadis.scriptspage.Content;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import org.jsoup.nodes.Document;

import java.io.File;

public class MainController {

    public final static String filesRootDirectory = System.getProperty("user.home");

    @FXML
    private TextField directoryTextField;

    @FXML
    private Button chooseDirectoryBtn;

    @FXML
    private Button syncBtn;

    @FXML
    private Label syncInfoLabel;

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
            Task<Void> task = new Task<Void>() {
                Synchronizer sync = new Synchronizer();

                @Override
                protected Void call() throws Exception {
                    sync.sync();
                    return null;
                }

                @Override
                protected void succeeded() {
                    System.out.println("succeeded");
                    for (Content content : sync.getLastAdded()) {
                        System.out.println(content.getName());
                    }
                }

                @Override
                protected void failed() {
                    super.failed();
                    System.out.println("failed");
                }
            };
            new Thread(task).start();
        });
    }

    protected void setSyncInfo(String text) {
        syncInfoLabel.setText(text);
    }

}
