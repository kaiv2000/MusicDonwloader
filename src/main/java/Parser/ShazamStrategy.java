package Parser;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.select.Elements;

import static Parser.LinksFinder.getSongLinkFromMp3nota;
import static Parser.LinksFinder.getSongLinkFromMp3xl;
import static Parser.MusicDownloader.deleteSmallFiles;

public class ShazamStrategy {

    public ShazamStrategy() throws IOException {
    }

    private static LinkedList<Track> trackListStarted = new LinkedList<>();
    private static CopyOnWriteArrayList<Track> trackListWithLinks = new CopyOnWriteArrayList<>();
    private static CopyOnWriteArrayList<Track> trackListWithoutLinks = new CopyOnWriteArrayList<>();
    private static HashSet<Track> trackListFinal = new HashSet<>();

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

    private static void getTracks(String url) throws IOException {

        String JSON_SOURCE = readJsonFromUrl(url).toString();
        String[] all = JSON_SOURCE.split("\"heading\":");
        List<String> list = new LinkedList<String>(Arrays.asList(all));
        list.remove(0);

        String[] array;
        for (String x : list) {
            array = x.split(":");
            String singer = array[1].split("\"")[1];
            String title = array[2].split("\"")[1];

            Track track = new Track();
            track.title = title;
            track.artist = singer;
            trackListStarted.add(track);
        }

    }

    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
        InputStream is = new URL(url).openStream();
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String jsonText = readAll(rd);
            JSONObject json = new JSONObject(jsonText);
            return json;
        } finally {
            is.close();
        }
    }

    private static void getLinksForTracks(List<Track> trackList, String musicServer) throws MalformedURLException {

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

    private static void copyTracksToListsByWorkedLinks(List<Track> trackList) throws IOException {

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
        FileWriter urlWriter = new FileWriter("D:\\DownloadedMusic\\musicLinksForDownloadMaster.urls", true);
        FileWriter songNamesWriter = new FileWriter("D:\\DownloadedMusic\\musicNames.txt", true);
        songNamesWriter.write("Count of founded links for: " + urlWithMusicTopChart + " - " + trackListFinal.size() + "\n");
        for (Track oneTrack : trackListFinal) {
            urlWriter.write(String.valueOf(oneTrack.url) + "\n");
            songNamesWriter.write(String.valueOf(oneTrack.artist + " - " + oneTrack.title) + "\n");
        }
        urlWriter.close();
        songNamesWriter.close();
    }

    private static void downloadFoundedTracks() throws IOException {
        for (Track oneTrack : trackListFinal) {
            MusicDownloader.downloadFile(oneTrack.url, oneTrack.artist, oneTrack.title, trackListFinal.size(), urlWithMusicTopChart);
        }
        deleteSmallFiles();
    }
}
