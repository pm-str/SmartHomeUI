package com.example.pm.smarthomeui;

import android.content.SharedPreferences;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

class HttpGETClient {
    static String main(String url, SharedPreferences pref) {
        String body = "";
        try {
            HttpGETClient hce = new HttpGETClient();
            body = hce.get(Utils.getHost(url, pref), pref);
            System.out.println(body);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return body;
    }

    private String get(String stringUrl, SharedPreferences pref) throws IOException {
        URL url = new URL(stringUrl);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con = Utils.getAuthToken(con, pref);

        return this.read(con.getInputStream());
    }

    private String read(InputStream is) throws IOException {
        BufferedReader in = null;
        String inputLine;
        StringBuilder body;
        try {
            in = new BufferedReader(new InputStreamReader(is));

            body = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                body.append(inputLine);
            }
            in.close();

            return body.toString();
        } finally {
            this.closeQuietly(in);
        }
    }

    private void closeQuietly(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (IOException ignored) {

        }
    }
}