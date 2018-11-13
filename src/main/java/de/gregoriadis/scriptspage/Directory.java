package de.gregoriadis.scriptspage;

import de.gregoriadis.WebScraper;
import org.jsoup.HttpStatusException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class Directory extends Content {

    /**
     * Contents inside directory
     */
    private List<Content> contents;

    /**
     * @return contents
     */
    public List<Content> getContents() throws IOException {
        if (contents == null) {
            contents = new ArrayList<>();
            Document document = WebScraper.getInstance().getDocumentFromURL(getUrl());

            // Only one table exists on this view
            Element firstTable = IhreSkripte.selectTables(document).get(0);
            // Get all contents
            Elements rows = IhreSkripte.selectRows(firstTable);
            for (Element row : rows) {
                Content content = IhreSkripte.getContentFromRow(row, getLocalPath(), false);
                contents.add(content);
            }
        }

        return contents;
    }

}
