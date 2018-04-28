package com.example.pm.smarthomeui;

import android.content.SharedPreferences;
import android.net.Uri;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;


class DataRequest {
    String makeRequest(String loginUrl, Uri.Builder builder, SharedPreferences preference,
                       String method, Boolean setDoOutput, Boolean setDoInput) throws IOException {
        String localhost = "http://192.168.43.145:8000/";
        String api = "api/v1/";

        URL url = new URL(localhost + api + loginUrl);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

        String token = preference.getString("token", "");

        urlConnection.setRequestMethod(method);
        if (token.length() > 0) {
            urlConnection.setRequestProperty("Authorization", "Token " + token);
        }

        urlConnection.setDoOutput(setDoOutput);
        urlConnection.setDoInput(setDoInput);
        if (setDoOutput) {
            //Getting object of OutputStream from urlConnection to write some data to stream
            OutputStream outputStream = urlConnection.getOutputStream();

            //Writer to write data to OutputStream
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
            bufferedWriter.write(builder.build().getEncodedQuery());
            bufferedWriter.flush();
            bufferedWriter.close();
            outputStream.close();
        }

        String data = "";

        if (setDoInput) {
            urlConnection.connect();
            //Getting input stream from connection, that is response which we got from server
            InputStream inputStream = urlConnection.getInputStream();
            //Reading the response
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            data = bufferedReader.readLine();

            bufferedReader.close();
            urlConnection.disconnect();
        }

        return data;
    }
}
