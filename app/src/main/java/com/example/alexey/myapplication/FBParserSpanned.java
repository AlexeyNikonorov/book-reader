package com.example.alexey.myapplication;

import android.graphics.Typeface;
import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.style.AlignmentSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import java.io.File;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

class FBParserSpanned {
    private static final int STATE_IGNORE = 0;
    private static final int STATE_READ = 1;
    private static final int STATE_KEEP_READING = 2;
    private static final int TITLE = 0;
    private static final int CITE = 1;
    private static final int EPIGRAPH = 2;
    private static final int POEM = 3;
    private static final int TEXT_AUTHOR = 4;
    private static final int P = 5;
    private static final int V = 6;
    private static final Map<String, Integer> tagCodes;
    private static final Set<String> acceptedTags;
    private static final Object[][] tagSpans;
    static {
        tagCodes = new HashMap<>();
        tagCodes.put("title", TITLE);
        tagCodes.put("cite", CITE);
        tagCodes.put("epigraph", EPIGRAPH);
        tagCodes.put("poem", POEM);
        tagCodes.put("text-author", TEXT_AUTHOR);

        acceptedTags = new HashSet<>();
        acceptedTags.add("text-author");
        acceptedTags.add("p");
        acceptedTags.add("v");

        tagSpans = new Object[5][];
        tagSpans[0] = new Object[3];
        tagSpans[0][0] = new StyleSpan(Typeface.BOLD);
        tagSpans[0][1] = new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER);
        tagSpans[0][2] = new RelativeSizeSpan(2.0f);
        tagSpans[1] = new Object[1];
        tagSpans[1][0] = new StyleSpan(Typeface.ITALIC);
        tagSpans[2] = new Object[2];
        tagSpans[2][0] = new StyleSpan(Typeface.ITALIC);
        tagSpans[2][1] = new AlignmentSpan.Standard(Layout.Alignment.ALIGN_OPPOSITE);
        tagSpans[3] = new Object[2];
        tagSpans[3][0] = new StyleSpan(Typeface.ITALIC);
        tagSpans[3][1] = new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER);
        tagSpans[4] = new Object[1];
        tagSpans[4][0] = new AlignmentSpan.Standard(Layout.Alignment.ALIGN_OPPOSITE);
    }
    SpannableStringBuilder text = new SpannableStringBuilder();

    void parse(File file) {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            SAXHandler saxHandler = new SAXHandler();
            parser.parse(file, saxHandler);
        } catch (Exception ex) {}
    }

    private class Pair {
        int start;
        Object[] spans;

        Pair(int start, Object[] spans) {
            this.start = start;
            this.spans = spans;
        }
    }

    private class SAXHandler extends DefaultHandler {
        private int state = STATE_IGNORE;
        private Deque<Pair> positions = new ArrayDeque<>();

        public void startElement(String uri, String localName, String qName, Attributes attributes) {
            Object[] spans = null;
            switch (qName) {
                case "p":
                    state = STATE_READ;
                    text.append('\t');
                    break;
                case "v":
                    state = STATE_READ;
                    break;
                case "text-author":
                    state = STATE_READ;
                    spans = new Object[1];
                    spans[0] = new AlignmentSpan.Standard(Layout.Alignment.ALIGN_OPPOSITE);
                    break;
                case "empty-line":
                    state = STATE_IGNORE;
                    break;
                case "title":
                    state = STATE_IGNORE;
                    text.append('\n');
                    spans = new Object[3];
                    spans[0] = new StyleSpan(Typeface.BOLD);
                    spans[1] = new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER);
                    spans[2] = new RelativeSizeSpan(2.0f);
                    break;
                case "cite":
                    state = STATE_IGNORE;
                    text.append('\n');
                    spans = new Object[1];
                    spans[0] = new StyleSpan(Typeface.ITALIC);
                    break;
                case "epigraph":
                    state = STATE_IGNORE;
                    text.append('\n');
                    spans = new Object[2];
                    spans[0] = new StyleSpan(Typeface.ITALIC);
                    spans[1] = new AlignmentSpan.Standard(Layout.Alignment.ALIGN_OPPOSITE);
                    break;
                case "poem":
                    state = STATE_IGNORE;
                    text.append('\n');
                    spans = new Object[2];
                    spans[0] = new StyleSpan(Typeface.ITALIC);
                    spans[1] = new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER);
                    break;
            }
            if (spans != null) {
                positions.push(new Pair(text.length(), spans));
            }
        }

        public void endElement(String uri, String localName, String qName) {
            switch (qName) {
                case "p":
                    state = STATE_IGNORE;
                    text.append('\n');
                    break;
                case "v":
                    state = STATE_IGNORE;
                    text.append('\n');
                    break;
                case "text-author":
                    state = STATE_IGNORE;
                    if (!positions.isEmpty()) {
                        Pair pair = positions.pop();
                        int start = pair.start;
                        int end = text.length();
                        for (Object span : pair.spans) {
                            text.setSpan(span, start, end, 0);
                        }
                    }
                    text.append('\n');
                    break;
                case "empty-line":
                    state = STATE_IGNORE;
                    text.append('\n');
                    break;
                case "title":
                    state = STATE_IGNORE;
                    if (!positions.isEmpty()) {
                        Pair pair = positions.pop();
                        int start = pair.start;
                        int end = text.length();
                        for (Object span : pair.spans) {
                            text.setSpan(span, start, end, 0);
                        }
                    }
                    text.append('\n');
                    break;
                case "cite":
                    state = STATE_IGNORE;
                    if (!positions.isEmpty()) {
                        Pair pair = positions.pop();
                        int start = pair.start;
                        int end = text.length();
                        for (Object span : pair.spans) {
                            text.setSpan(span, start, end, 0);
                        }
                    }
                    text.append('\n');
                    break;
                case "epigraph":
                    state = STATE_IGNORE;
                    if (!positions.isEmpty()) {
                        Pair pair = positions.pop();
                        int start = pair.start;
                        int end = text.length();
                        for (Object span : pair.spans) {
                            text.setSpan(span, start, end, 0);
                        }
                    }
                    text.append('\n');
                    break;
                case "poem":
                    state = STATE_IGNORE;
                    if (!positions.isEmpty()) {
                        Pair pair = positions.pop();
                        int start = pair.start;
                        int end = text.length();
                        for (Object span : pair.spans) {
                            text.setSpan(span, start, end, 0);
                        }
                    }
                    text.append('\n');
                    break;
            }
        }

        public void characters(char[] ch, int start, int length) {
            switch (state) {
                case STATE_READ:
                    state = STATE_KEEP_READING;
                case STATE_KEEP_READING:
                    String chunk = new String(ch, start, length);
                    text.append(chunk);
            }
        }
    }
}