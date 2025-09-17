package com.example.ebookreader.api;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class Book  implements Serializable {
    public int id;
    public String title;
    public List<Author> authors;
    public List<String> subjects;
    public List<String> summaries;
    public Formats formats;

    public static class Author implements Serializable {
        public String name;
    }

    public static class Formats implements Serializable {
        @SerializedName("application/pdf")
        public String applicationPdf;

        @SerializedName("text/plain; charset=us-ascii")
        public String textPlain;

        @SerializedName("application/epub+zip")
        public String epub;

        @SerializedName("text/html")
        public String html;


    }
}