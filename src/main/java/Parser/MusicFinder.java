package Parser;

import java.io.IOException;

public class MusicFinder {

    public static void main(String[] args) throws IOException {
        ShazamStrategy.start("https://www.shazam.com/shazam/v2/en/UA/web/-/tracks/web_chart_world");
    }
}