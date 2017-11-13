package com.example.alexey.myapplication;

import java.util.List;
import java.util.ArrayList;

public class Article {
    private String word;
    private List<String> variants;

    public Article() {
        variants = new ArrayList<String>();
    }

    public void setWord(String word) {
        this.word = word;
    }

    public void addVariant(String variant) {
        variants.add(variant);
    }

    public String toString() {
        String res = word + ":\n\t" + variants.get(0);
        for (int i = 1; i < variants.size(); i++) {
            res += "\n\t" + variants.get(i);
        }
        return res;
    }
}