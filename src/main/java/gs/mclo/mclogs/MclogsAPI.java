package gs.mclo.mclogs;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.zip.GZIPInputStream;

public class MclogsAPI {
    public static String mcversion = "unknown";
    public static String userAgent = "unknown";
    public static String version = "unknown";

    private static String inputStreamToString (InputStream is) throws IOException {
        BufferedReader buf = new BufferedReader(new InputStreamReader(is));
        String line = buf.readLine();
        StringBuilder sb = new StringBuilder();
        while (line != null) {
            sb.append(line).append("\n");
            line = buf.readLine();
        }
        return sb.toString();
    }

    public static APIResponse share(String file) throws IOException {
        //read log
        InputStream LogIS = new FileInputStream(new File(file));

        //decompress log
        if (file.endsWith(".gz")) {
            LogIS = new GZIPInputStream(LogIS);
        }

        //read log to string
        String log = inputStreamToString(LogIS);

        //connect to api
        URL url = new URL("https://api.mclo.gs/1/log");
        URLConnection con = url.openConnection();
        HttpURLConnection http = (HttpURLConnection) con;
        http.setRequestMethod("POST");
        http.setDoOutput(true);

        //convert log to application/x-www-form-urlencoded
        String content = "content=" + URLEncoder.encode(log, StandardCharsets.UTF_8.toString());
        byte[] out = content.getBytes(StandardCharsets.UTF_8);
        int length = out.length;

        //send log to api
        http.setFixedLengthStreamingMode(length);
        http.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        http.setRequestProperty("User-Agent", userAgent + "/" + version + "/" + mcversion);
        http.connect();
        try (OutputStream os = http.getOutputStream()) {
            os.write(out);
        }

        //handle response
        return APIResponse.parse(inputStreamToString(http.getInputStream()));
    }

    public static String[] listLogs(String rundir){

        File logdir = new File(rundir + "/logs");

        String[] logs = logdir.list();
        if (logs == null)
            logs = new String[0];

        ArrayList<String> logsList = new ArrayList<>();
        for (String log:logs) {
            if (log.endsWith(".log")||log.endsWith(".log.gz"))
                logsList.add(log);
        }

        Collections.sort(logsList);
        return logsList.toArray(new String[0]);
    }
}