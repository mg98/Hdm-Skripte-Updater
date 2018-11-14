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
        String username = Config.getInstance().getUsername();
        String password = Config.getInstance().getPassword();

        if (!username.equals("") && !password.equals("")) {
            login();
        }

        usernameTextField.setText(username);
        passwordField.setText(password);

        loginBtn.setOnMouseClicked(t -> {
            Main.saveLogin(usernameTextField.getText(), passwordField.getText());

            if (usernameTextField.getText().equals("") || passwordField.getText().equals("")) {
                setStatusMessage("Die Felder dürfen HdM-Kürzel und Passwort nicht leer sein.");
            } else {
                login();
            }
        });
    }

    protected void login() {
        try {
            WebScraper.newInstance().getDocumentFromURL(Main.baseURL);
            // Login!
            Main.switchToMainScene();
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
    }

    protected void setStatusMessage(String text) {
        status.setText(text);
    }

}
