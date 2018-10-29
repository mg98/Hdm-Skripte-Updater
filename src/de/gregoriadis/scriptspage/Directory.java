package de.gregoriadis.scriptspage;

import de.gregoriadis.WebScraper;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.List;

public class Directory extends Content {

    private List<Content> contents;

    protected List<Content> getContents() {
        if (contents == null) {
            Document document = WebScraper.getInstance().getDocumentFromURL(getUrl());

            // Only one table exists on this view
            Element firstTable = IhreSkripte.selectTables(document).get(0);
            // Get all contents
            Elements rows = IhreSkripte.selectRows(firstTable);
            for (Element row : rows) {
                Content content = IhreSkripte.getContentFromRow(row);
                contents.add(content);
            }
        }

        return contents;
    }

}
