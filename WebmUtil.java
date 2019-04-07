import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collection;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Objects.nonNull;

public class WebmUtil {

    private final static String REGEX = "(?<=(href=\"))([a-zA-Z0-9/-_]+.(webm|mp4))";
    private String path = "/home/smile/Downloads/2ch-webm";
    private int videosCount;
    private int currentVideoCount;
    private String page = "https://2ch.hk/b/res/194253637.html";

    public static void main(String[] args) throws IOException {
        WebmUtil webmUtil = new WebmUtil();
        webmUtil.initArgs(args);
        String page = webmUtil.getPage();
        webmUtil.downloadVideos(webmUtil.getLinks(page));
    }

    private void initArgs(String[] args) {
        if (args.length == 2) {
            path = args[0];
            page = args[1];
        }
    }

    private void downloadVideos(Collection<String> links) {
        File file = new File(path);
        if (!file.exists()) file.mkdir();
        for (String link : links) {
            downloadFile(link, path);
        }
    }

    private void downloadFile(String link, String path) {
        InputStream in = null;
        FileOutputStream out = null;
        long start = System.currentTimeMillis();
        try {
            URL url = new URL(link);
            URLConnection conn = url.openConnection();
            int size = conn.getContentLength();
            in = new BufferedInputStream(url.openStream());
            out = new FileOutputStream(path + getFileName(link));
            byte data[] = new byte[4096];
            int count;
            double sumCount = 0.0;
            String info;
            while ((count = in.read(data, 0, 1024)) != -1) {
                out.write(data, 0, count);
                sumCount += count;
                if (size > 0) {
                    info = "\r"
                            + "VIDEO:" + getFileName(link)
                            + " [" + size / 1000000 + " MB| "
                            + ((System.currentTimeMillis() - start) / 1000) + "s| "
                            + currentVideoCount + "/" + videosCount + " |"
                            + (int) (sumCount / size * 100.0) + "%]"
                            + "                       ";


                    System.out.print(info);
                }
            }
            currentVideoCount++;
            System.out.println();
        } catch (IOException e) {
            System.err.println("Cannot download file from:" + link + ". " + e.toString());
        } finally {
            try {
                if (nonNull(in)) in.close();
                if (nonNull(out)) out.close();
            } catch (IOException e) {

            }
        }
    }

    private String getFileName(String link) {
        int length = link.length();
        return link.substring(length - 15, length);
    }

    private String getPage() {
        URL url = null;
        InputStream is = null;
        BufferedReader br;
        StringBuilder html = new StringBuilder();
        try {
            url = new URL(page);
            is = url.openStream();
            br = new BufferedReader(new InputStreamReader(is));
            br.lines().forEach(html::append);
        } catch (IOException e) {
            System.err.println("Cannot read url:" + url);
        } finally {
            try {
                if (nonNull(is)) is.close();
            } catch (IOException e) {

            }
        }
        return html.toString();
    }

    private Collection<String> getLinks(String page) {
        Pattern pattern = Pattern.compile(REGEX);
        Matcher matcher = pattern.matcher(page);
        HashSet<String> result = new HashSet<>();
        while (matcher.find()) {
            result.add("https://2ch.hk" + matcher.group());
        }
        videosCount = result.size();
        System.out.println("Videos count: " + videosCount);
        return result;
    }
}
