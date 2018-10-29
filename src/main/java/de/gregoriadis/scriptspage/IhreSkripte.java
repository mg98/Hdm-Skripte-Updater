package de.gregoriadis.scriptspage;

import de.gregoriadis.Main;
import de.gregoriadis.WebScraper;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class IhreSkripte {

    private List<Course> courses = new ArrayList<>();

    public IhreSkripte() {
        Document document = WebScraper.getInstance().getDocumentFromURL(Main.baseURL);

        Elements downloads = document.select(".content h2 a");
        Elements tables = selectTables(document);
        // Each course
        int i = 0;
        for (Element table : tables) {
            Element download = downloads.get(i);
            Course course = new Course();
            // Get course name
            String courseName = download.parent().ownText();
            courseName = courseName.substring(8, courseName.length() - 2);
            course.setName(courseName);
            course.setZipDownloadUrl(Main.baseURL + download.attr("href"));

            // Each content
            Elements rows = selectRows(table);
            for (Element row : rows) {
                course.addContent(getContentFromRow(row, courseName));
            }

            courses.add(course);
            i++;
        }

    }

    public List<Course> getCourses() {
        return courses;
    }

    protected static Elements selectTables(Document document) {
        return document.select(".content > table.tablestyle2");
    }

    protected static Elements selectRows(Element table) {
        return table.select("tr:not(:first-child)");
    }

    protected static Content getContentFromRow(Element row, String localPath) {
        Elements tds = row.getElementsByTag("td");

        Element contentLink = tds.get(0).getElementsByTag("a").get(0);
        String name = contentLink.ownText();
        String url = Main.baseURL + contentLink.attr("href");

        String updatedAtString = tds.get(1).ownText();
        DateTimeFormatter formatter = DateTimeFormat.forPattern("dd.MM.yyyy HH:mm");
        DateTime updatedAt = formatter.parseDateTime(updatedAtString);
        boolean directory = tds.get(3).ownText().equals("directory");

        Content content;
        // Directory
        if (directory) content = new Directory();
            // File
        else content = new File();
        content.setName(name);
        content.setUpdatedAt(updatedAt);
        content.setUrl(url);
        content.setLocalPath(localPath + "/" + name);

        return content;
    }

}
