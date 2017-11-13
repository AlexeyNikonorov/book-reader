package com.example.alexey.myapplication;

import android.graphics.Typeface;
import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.style.AlignmentSpan;
import android.text.style.LeadingMarginSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import java.io.File;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

class FBParserSpanned2 {
    private static final int STATE_IGNORE = 0;
    private static final int STATE_READ = 1;
    private static final int STATE_KEEP_READING = 2;
    SpannableStringBuilder text = new SpannableStringBuilder();

    void parse(File file) {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            SAXHandler saxHandler = new SAXHandler();
            parser.parse(file, saxHandler);
        } catch (Exception ex) {}
    }

    private interface TagHandler {
        void onStart();
        void onEnd();
    }

    private class SAXHandler extends DefaultHandler {
        private int state = STATE_IGNORE;
        private Deque<Integer> positions = new ArrayDeque<>();
        private Map<String, TagHandler> tagHandlers = new HashMap<>();

        SAXHandler() {
            tagHandlers.put("p", new PHandler());
            tagHandlers.put("v", new VHandler());
            tagHandlers.put("stanza", new StanzaHandler());
            tagHandlers.put("text-author", new TextAuthorHandler());
            tagHandlers.put("empty-line", new EmptyLineHandler());
            tagHandlers.put("title", new TitleHandler());
            tagHandlers.put("cite", new CiteHandler());
            tagHandlers.put("epigraph", new EpigraphHandler());
            tagHandlers.put("poem", new PoemHandler());
        }

        private class PHandler implements TagHandler {
            public void onStart() {
                state = STATE_READ;
                text.append('\t');
            }

            public void onEnd() {
                state = STATE_IGNORE;
                text.append('\n');
            }
        }

        private class VHandler implements TagHandler {
            public void onStart() {
                state = STATE_READ;
            }

            public void onEnd() {
                state = STATE_IGNORE;
                text.append('\n');
            }
        }

        private class StanzaHandler implements TagHandler {
            public void onStart() {
                state = STATE_IGNORE;
            }

            public void onEnd() {
                state = STATE_IGNORE;
                text.append('\n');
            }
        }

        private class TextAuthorHandler implements TagHandler {
            public void onStart() {
                state = STATE_READ;
                positions.push(text.length());
            }

            public void onEnd() {
                state = STATE_IGNORE;
                int start = positions.pop();
                int end = text.length();
                text.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_OPPOSITE), start, end, 0);
                text.append('\n');
            }
        }

        private class EmptyLineHandler implements TagHandler {
            public void onStart() {
                state = STATE_IGNORE;
            }

            public void onEnd() {
                state = STATE_IGNORE;
                text.append('\n');
            }
        }

        private class TitleHandler implements TagHandler {
            public void onStart() {
                state = STATE_IGNORE;
                text.append('\n');
                positions.push(text.length());
            }

            public void onEnd() {
                state = STATE_IGNORE;
                int start = positions.pop();
                int end = text.length();
                text.setSpan(new StyleSpan(Typeface.BOLD), start, end, 0);
                text.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), start, end, 0);
                text.setSpan(new RelativeSizeSpan(2.0f), start, end, 0);
                text.append('\n');
            }
        }

        private class CiteHandler implements TagHandler {
            public void onStart() {
                state = STATE_IGNORE;
                text.append('\n');
                positions.push(text.length());
            }

            public void onEnd() {
                state = STATE_IGNORE;
                int start = positions.pop();
                int end = text.length();
                text.setSpan(new StyleSpan(Typeface.ITALIC), start, end, 0);
                text.setSpan(new LeadingMarginSpan.Standard(20), start, end, 0);
                text.append('\n');
            }
        }

        private class EpigraphHandler implements TagHandler {
            public void onStart() {
                state = STATE_IGNORE;
                text.append('\n');
                positions.push(text.length());
            }

            public void onEnd() {
                state = STATE_IGNORE;
                int start = positions.pop();
                int end = text.length();
                text.setSpan(new StyleSpan(Typeface.ITALIC), start, end, 0);
                text.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_OPPOSITE), start, end, 0);
                text.append('\n');
            }
        }

        private class PoemHandler implements TagHandler {
            public void onStart() {
                state = STATE_IGNORE;
                text.append('\n');
                positions.push(text.length());
            }

            public void onEnd() {
                state = STATE_IGNORE;
                int start = positions.pop();
                int end = text.length();
                text.setSpan(new StyleSpan(Typeface.ITALIC), start, end, 0);
                text.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), start, end, 0);
                text.append('\n');
            }
        }

        public void startElement(String uri, String localName, String qName, Attributes attributes) {
            TagHandler tagHandler = tagHandlers.get(qName);
            if (tagHandler != null) {
                tagHandler.onStart();
            }
        }

        public void endElement(String uri, String localName, String qName) {
            TagHandler tagHandler = tagHandlers.get(qName);
            if (tagHandler != null) {
                tagHandler.onEnd();
            }
        }

        public void characters(char[] ch, int start, int length) {
            switch (state) {
                case STATE_READ:
                    state = STATE_KEEP_READING;
                case STATE_KEEP_READING:
                    text.append(new String(ch, start, length));
            }
        }
    }
}