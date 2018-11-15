package de.gregoriadis;

import de.gregoriadis.Config;
import de.gregoriadis.Main;
import de.gregoriadis.WebScraper;
import de.gregoriadis.scriptspage.Content;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.awt.*;
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
        Main.switchToLoadingScene();

        new Thread(new Task<Void>() {

            @Override
            protected Void call() {
                try {
                    WebScraper.newInstance().getDocumentFromURL(Main.baseURL);
                    return null;
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

                Platform.runLater(() -> Main.switchToLoginScene());

                return null;
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> Main.switchToMainScene());
            }

            @Override
            protected void failed() {
                super.failed();
                Platform.runLater(() -> Main.switchToLoginScene());
            }

        }).start();
    }

    protected void setStatusMessage(String text) {
        status.setText(text);
    }

}
