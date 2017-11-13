package com.example.alexey.myapplication;

import android.util.Log;

import javax.net.ssl.HttpsURLConnection;

import java.io.BufferedInputStream;
import java.net.URL;
import java.lang.Exception;
import java.io.InputStream;

public class Dictionary {
    static final private String link = "https://dictionary.yandex.net/api/v1/dicservice/lookup?key=dict.1.1.20171025T104134Z.09613d4459fd973d.7c3823120d2d9083edb00f30e25241ea616dd8ad&lang=en-ru&text=";
    private ArticleParser articleParser;

    public Dictionary() {
        articleParser = new ArticleParser();
    }

    public Article getArticle(String entry) {
        Article article = null;
        try {
            URL url = new URL(link + entry);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            InputStream response = connection.getInputStream();
            article = articleParser.parse(response);
        } catch (Exception exception) {}
        return article;
    }
}