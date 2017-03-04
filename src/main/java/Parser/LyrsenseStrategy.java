package Parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static Parser.LinksFinder.getSongLinkFromMp3nota;
import static Parser.LinksFinder.getSongLinkFromMp3xl;
import static Parser.MusicDownloader.deleteSmallFiles;

public class LyrsenseStrategy{

    public LyrsenseStrategy() throws IOException {
    }

    private static LinkedList<Track> trackListStarted = new LinkedList<>();
    private static CopyOnWriteArrayList<Track> trackListWithLinks = new CopyOnWriteArrayList<>();
    private static CopyOnWriteArrayList<Track> trackListWithoutLinks = new CopyOnWriteArrayList<>();
    private static HashSet<Track> trackListFinal = new HashSet<>();

    private static Elements elements;
    private static int counter = 1;
    private static String urlWithMusicTopChart = "";

    static void start(String incomingUrl) throws IOException {
        urlWithMusicTopChart = incomingUrl;
        new File("D:\\DownloadedMusic").mkdir();
        getTracks(urlWithMusicTopChart);
        getLinksForTracks(trackListStarted, "mp3nota");
        copyTracksToListsByWorkedLinks(trackListStarted);
        getLinksForTracks(trackListWithoutLinks, "mp3xl");
        copyTracksToListsByWorkedLinks(trackListWithoutLinks);
        createFileWithUrls();
        downloadFoundedTracks();
    }

    public static void getTracks(String url) throws IOException {

        Document document = Jsoup.connect(url).userAgent("Mozilla/5.0 jsoup").referrer("www.google.com.ua").get();
        elements = document.getElementsByClass("hitParadContentRow");

        if (elements.size() != 0) {
            for (Element oneSong : elements) {
                Element titleElement = oneSong.select("[class=songTitle gotha blackLink]").first();
                Element artistElement = oneSong.select("[class=gotha greyText]").first();

                String songArtist = "";
                String songTitle = "";

                Track track = new Track();
                if (artistElement != null) {
                    songArtist = artistElement.text();
                    track.artist = songArtist;
                } else {
                    track.artist = "";
                }

                if (titleElement != null) {
                    songTitle = titleElement.text();
                    track.title = songTitle;
                } else {
                    track.title = "";
                }

                track.counter = counter++;
                if (!(track.title.isEmpty() || track.artist.isEmpty())) {
                    trackListStarted.add(track);
                }
            }
        } else {
            System.out.println("Nothing found at: lyrsense.com/hitParad :(");
        }
    }

    public static void getLinksForTracks(List<Track> trackList, String musicServer) throws MalformedURLException {

        for (Track oneTrack : trackList) {
            URL songLink = new URL("http://noLink=(");
            String songArtist = oneTrack.artist;
            String songTitle = oneTrack.title;
            String searchSongString = songArtist + " - " + songTitle;

            try {
                if (musicServer.equals("mp3nota")) {
                    songLink = getSongLinkFromMp3nota(searchSongString);
                }
                if (musicServer.equals("mp3xl")) {
                    songLink = getSongLinkFromMp3xl(searchSongString);
                }
                oneTrack.url = songLink;
            } catch (Exception e) {
                oneTrack.url = songLink;
            }
        }
    }

    public static void copyTracksToListsByWorkedLinks(List<Track> trackList) throws IOException {

        for (Track oneTrack : trackList) {
            String checkedUrl = String.valueOf(oneTrack.url);
            if (checkedUrl.contains("http://noLink=(")) {
                trackListWithoutLinks.add(oneTrack);
            } else {
                trackListWithLinks.add(oneTrack);
            }
        }
        trackListFinal.addAll(trackListWithLinks);
    }

    private static void createFileWithUrls() throws IOException {
        FileWriter writer = new FileWriter("D:\\musicLinksForDownloadMaster.urls", true);
        FileWriter writerNames = new FileWriter("D:\\musicNames.txt", true);
        writerNames.write("Count of founded links for: " + urlWithMusicTopChart + " - " + trackListFinal.size() + "\n");
        for (Track oneTrack : trackListFinal) {
            writer.write(String.valueOf(oneTrack.url) + "\n");
            writerNames.write(String.valueOf(oneTrack.artist + " - " + oneTrack.title) + "\n");
        }
        writer.close();
        writerNames.close();
    }

    private static void downloadFoundedTracks() throws IOException {
        for (Track oneTrack : trackListFinal) {
            MusicDownloader.downloadFile(oneTrack.url, oneTrack.artist, oneTrack.title, trackListFinal.size(), urlWithMusicTopChart);
        }
        deleteSmallFiles();
    }
}
