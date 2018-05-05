package com.example.pm.smarthomeui;

import android.content.SharedPreferences;
import android.net.Uri;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

class HttpPOSTClient {

    static String main(String url, Uri.Builder builder, SharedPreferences pref) {
        String body = "";
        try {
            HttpPOSTClient hce = new HttpPOSTClient();
            body = hce.post(Utils.getHost(url, pref), builder, pref);
            System.out.println(body);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return body;
    }

    private String post(String postUrl, Uri.Builder builder, SharedPreferences pref) throws IOException {
        URL url = new URL(postUrl);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con = Utils.getAuthToken(con, pref);

        con.setDoOutput(true);
        this.sendData(con, builder);
        InputStream ls = con.getInputStream();

        return this.read(ls);
    }

    private void sendData(HttpURLConnection con, Uri.Builder builder) throws IOException {
        DataOutputStream wr = null;
        try {
            wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(builder.build().getEncodedQuery());
            wr.flush();
            wr.close();
        } finally {
            this.closeQuietly(wr);
        }
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
            is.close();
            return body.toString();
        } finally {
            this.closeQuietly(in);
            this.closeQuietly(is);
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

