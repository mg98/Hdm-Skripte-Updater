package de.gregoriadis;

import de.gregoriadis.Config;
import de.gregoriadis.Main;
import de.gregoriadis.WebScraper;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.UnknownHostException;

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


            try {
                Document doc = WebScraper.newInstance().getDocumentFromURL(Main.baseURL);
                if (doc != null) {
                    // Login!
                    Main.switchToMainScene();
                }
            }
            catch (HttpStatusException e) {
                switch (e.getStatusCode()) {
                    case 401:
                        setStatusMessage("Nutzername oder Passwort ist falsch.");
                        break;
                    default:
                        setStatusMessage("HTTP " + e.getStatusCode());
                }
            }
            catch (UnknownHostException e) {
                setStatusMessage("Could not establish connection with hdm website. Check your internet connection!");
            }
            catch (IOException e) {
                setStatusMessage("Dokument konnte nicht gefetcht werden.");
                e.printStackTrace();
            }
        });
    }

    protected void setStatusMessage(String text) {
        status.setText(text);
    }

}
