package de.gregoriadis;

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
            Main.downloadEverything();
        });
    }

}
