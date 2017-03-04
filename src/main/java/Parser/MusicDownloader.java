package Parser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

public class MusicDownloader {

    static int counterOfFailedDownloads = 0;
    static int counterOfSuccessDownloads = 0;

    public static void downloadFile(URL url, String artist, String title, int songsCount, String urlWithMusicTopChart) throws IOException {

        try {
            File downloadsLogFile = new File("D:\\DownloadedMusic\\DownloadCounterLog.txt");
            downloadsLogFile.createNewFile();
            FileWriter fileWriterFailedDow = new FileWriter(downloadsLogFile);
            fileWriterFailedDow.write("Total files for download from " + urlWithMusicTopChart + ": " + songsCount + "\n");
            fileWriterFailedDow.write("Successfully downloaded files: " + counterOfSuccessDownloads + "\n");
            fileWriterFailedDow.write("Failed to download files: " + counterOfFailedDownloads);
            fileWriterFailedDow.close();

            new File("D:\\DownloadedMusic\\Music").mkdir();
            String downloadedFilesPath = String.format("D:\\DownloadedMusic\\Music\\%s-%s.mp3", artist, title);
            new File(downloadedFilesPath).createNewFile();

            ReadableByteChannel channel = Channels.newChannel(url.openStream());
            FileOutputStream fileOutputStream = new FileOutputStream(downloadedFilesPath);
            fileOutputStream.getChannel().transferFrom(channel, 0, Long.MAX_VALUE);
            
			fileOutputStream.close();
            counterOfSuccessDownloads++;
        } catch (IOException e)
        {
            System.out.println("Music Server not respond...");
            counterOfFailedDownloads++;
        }

    }

    public static void deleteSmallFiles() {
        File folderWithMusic = new File("D:\\DownloadedMusic\\Music");
        for (File oneFile : folderWithMusic.listFiles()) {
            if ((oneFile.length() / 1024) < 2048) {
                oneFile.delete();
            }
        }
    }

}
