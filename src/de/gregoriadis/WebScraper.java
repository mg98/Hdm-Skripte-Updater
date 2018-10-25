package de.gregoriadis;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.*;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Base64;
import java.util.HashMap;

public class WebScraper {

    private static WebScraper instance = null;
    private String base64login;
    private HashMap<String, Document> cache = new HashMap<>();


    public WebScraper() {
        this.base64login = new String(Base64.getEncoder().encode((Config.getInstance().getUsername() + ":" + Config.getInstance().getPassword()).getBytes()));

        Authenticator.setDefault(new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(Config.getInstance().getUsername(), Config.getInstance().getPassword().toCharArray());
            }
        });
    }

    public Document getDocumentFromURL(String url) {
        try {
            if (cache.containsKey(url)) {
                return cache.get(url);
            }

            Document document = Jsoup
                    .connect(url)
                    .header("Authorization", "Basic " + base64login)
                    .get();
            cache.put(url, document);

            return document;
        }
        catch (HttpStatusException e) {
            switch (e.getStatusCode()) {
                case 401:
                    Main.getLoginController().setStatusMessage("Nutzername oder Passwort ist falsch.");
                    break;
                default:
                    Main.getLoginController().setStatusMessage("HTTP " + e.getStatusCode());
            }
        }
        catch (UnknownHostException e) {
            Main.getLoginController().setStatusMessage("Could not establish connection with hdm website. Check your internet connection!");
        }
        catch (IOException e) {
            Main.getLoginController().setStatusMessage("Dokument konnte nicht gefetcht werden.");
            e.printStackTrace();
        }

        return null;
    }

    public void download(String url, String location) {
        try {
            URL website = new URL(url);
            ReadableByteChannel rbc = Channels.newChannel(website.openStream());
            FileOutputStream fos = new FileOutputStream(location);
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void clearCache() {
        cache.clear();
    }

    public static WebScraper getInstance() {
        if (instance == null) instance = new WebScraper();

        return instance;
    }

}