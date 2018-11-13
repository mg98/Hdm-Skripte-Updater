package de.gregoriadis;

import de.gregoriadis.Config;
import de.gregoriadis.Main;
import de.gregoriadis.WebScraper;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.jsoup.nodes.Document;

public class LoginController {

    @FXML
    private TextField usernameTextField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginBtn;

    @FXML
    private Label status;

    @FXML
    protected void initialize() {
        usernameTextField.setText(Config.getInstance().getUsername());
        passwordField.setText(Config.getInstance().getPassword());

        loginBtn.setOnMouseClicked(t -> {
            Main.saveLogin(usernameTextField.getText(), passwordField.getText());
            // Test connection
            Document doc = WebScraper.newInstance().getDocumentFromURL(Main.baseURL);
            if (doc != null) {
                Main.switchToMainScene();
            }
        });
    }

    protected void setStatusMessage(String text) {
        status.setText(text);
    }

}
