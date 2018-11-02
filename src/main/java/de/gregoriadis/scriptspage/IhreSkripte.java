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

/**
 * Responsible for operations concerning the DOM of the hdm scripts page,
 * basically fetching needed data
 */
public class IhreSkripte {

    /**
     * All courses and their root level content items
     */
    private List<Course> courses = new ArrayList<>();

    /**
     * Creating an instance of this object will automatically fetch the data from the website
     */
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
                course.addContent(getContentFromRow(row, courseName, true));
            }

            courses.add(course);
            i++;
        }

    }

    /**
     * @return courses
     */
    public List<Course> getCourses() {
        return courses;
    }


    /**
     * Returns the html tables of the document that will represent the directory
     *
     * @param document HTML document of webpage
     * @return HTML table nodes
     */
    protected static Elements selectTables(Document document) {
        return document.select(".content > table.tablestyle2");
    }

    /**
     * Returns the rows of the table, ignoring the header row, that will
     * represent the files and directories in that directory
     *
     * @param table HTML table
     * @return HTML row nodes
     */
    protected static Elements selectRows(Element table) {
        return table.select("tr:not(:first-child)");
    }

    /**
     * Fetch a content object from a html table row
     *
     * @param row HTML table row
     * @param localPath Local path relative to sync directory to store the content in
     * @param root Content comes from the root of a course or a sub-directory
     * @return
     */
    protected static Content getContentFromRow(Element row, String localPath, boolean root) {
        Elements tds = row.getElementsByTag("td");

        Element contentLink = tds.get(0).getElementsByTag("a").get(0);
        String name = contentLink.ownText();
        // Get url from href and replace whitespaces
        String url = contentLink.attr("href").replaceAll("\\s", "%20");
        if (!url.startsWith("https://")) url = Main.baseURL + url;

        String updatedAtString = tds.get(1).ownText();
        DateTimeFormatter formatter = DateTimeFormat.forPattern(root ? "dd.MM.yyyy HH:mm" : "HH:mm:ss yyyy/MM/dd");
        DateTime updatedAt = formatter.parseDateTime(updatedAtString);
        boolean directory = tds.get(3).ownText().equals("directory") || tds.get(3).ownText().equals("Ordner");

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
