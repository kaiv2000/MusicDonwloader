package Parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;

public class LinksFinder {

    private static final String URL_FORMAT_MP3NOTA = "http://mp3nota.com/poisk?q=%s";
    private static final String URL_FORMAT_MP3XL = "http://mp3xl.org/search/?query=%s";

    public static URL getSongLinkFromMp3nota(String searhString) throws IOException {

        String url = String.format(URL_FORMAT_MP3NOTA, searhString);
        Document document = Jsoup.connect(url).userAgent("Mozilla/5.0 jsoup").referrer("www.google.com.ua").get();
        Element artistElement = document.select("[class=song]").first();
        Element urlElement = artistElement.select("a").first();
        String songInternalPath = urlElement.attr("href");
        int pathLength = songInternalPath.split("/").length;
        String internalSongName = songInternalPath.split("/")[pathLength - 1];
        URL songLink = new URL("http://mp3nota.com/dwn/" + internalSongName);
        return songLink;
    }

    public static URL getSongLinkFromMp3xl(String searhString) throws IOException {

        String url = String.format(URL_FORMAT_MP3XL, searhString);
        Document document = Jsoup.connect(url).userAgent("Mozilla/5.0 jsoup").referrer("www.google.com.ua").get();
        Element trackClassElement = document.select("[class=table ]").first();
        Elements trackLinks = trackClassElement.select("a[href]");

        String internaSiteSongName = "";
        URL songLink = new URL("http://mp3xl.org");

        for (Element element : trackLinks) {
            if (element.attr("href").endsWith(".mp3")) {
                internaSiteSongName = element.attr("href");
                break;
            }
        }
        if (internaSiteSongName.contains("store.naiti"))
        {
            songLink = new URL(internaSiteSongName);
        }
        else
        {
            songLink = new URL("http://mp3xl.org" + internaSiteSongName);
        }
        return songLink;
    }
}
