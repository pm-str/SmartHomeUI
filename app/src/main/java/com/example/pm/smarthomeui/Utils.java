package com.example.pm.smarthomeui;

import android.content.SharedPreferences;

import java.net.HttpURLConnection;

class Utils {
    static String getHost(String slug) {
        String localhost = "http://192.168.1.44:8000/";

        String api = "api/v1/";
        return localhost + api + slug;
    }

    static HttpURLConnection getAuthToken(HttpURLConnection conn, SharedPreferences preference) {
        String token = preference.getString("token", "");
        if (token.length() > 0) {
            conn.setRequestProperty("Authorization", "Token " + token);
        }
        return conn;
    }
}
