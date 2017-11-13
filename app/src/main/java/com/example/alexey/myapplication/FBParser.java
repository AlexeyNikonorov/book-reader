package com.example.alexey.myapplication;

import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.Attributes;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;
import java.io.File;
import java.lang.StringBuilder;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class FBParser {
    private static final int TITLE = 1;
    private static final int CITE = 2;
    private static final int EPIGRAPH = 4;
    private static final int POEM = 8;
    private static final int TEXT_AUTHOR = 16;
    private static final int OPEN = 32;
    private static final int CLOSE = 64;
    private static final Map<String,Integer> tagCodes;
    static {
        tagCodes = new HashMap<>();
        tagCodes.put("title", TITLE);
        tagCodes.put("cite", CITE);
        tagCodes.put("epigraph", EPIGRAPH);
        tagCodes.put("poem", POEM);
        tagCodes.put("text-author", TEXT_AUTHOR);
    }
    List<Element> items = new ArrayList<>();

    public void parse(File file) {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            SAXHandler saxHandler = new SAXHandler();
            parser.parse(file, saxHandler);
        } catch (Exception ex) {}
    }

    private class SAXHandler extends DefaultHandler {
        int mask;
        StringBuilder text;

        public void startDocument() {
            mask = 0;
        }

        public void startElement(String uri, String localName, String qName, Attributes attributes) {
            switch (qName) {
                case "p":
                case "v":
                    text = new StringBuilder();
                    break;
                case "text-author":
                    text = new StringBuilder();
                default:
                    Integer code = tagCodes.get(qName);
                    if (code != null) {
                        mask |= code;
                    }
            }
        }

        public void endElement(String uri, String localName, String qName) {
            switch (qName) {
                case "p":
                case "v":
                    items.add(new Element(mask, text.toString()));
                    break;
                case "empty-line":
                    items.add(new Element(0, "\n"));
                    break;
                case "text-author":
                    items.add(new Element(mask, text.toString()));
                default:
                    Integer code = tagCodes.get(qName);
                    if (code != null) {
                        mask &= ~code;
                    }
            }
        }

        public void characters(char[] ch, int start, int length) {
            if (text != null) {
                text.append(ch, start, length);
            }
        }

        public String toString() {
            if (items.isEmpty()) {
                return "";
            }
            StringBuilder stringBuilder = new StringBuilder(items.get(0).toString());
            for (int i = 1; i < items.size(); i++) {
                stringBuilder.append("\n");
                stringBuilder.append(items.get(i).toString());
            }
            return stringBuilder.toString();
        }
    }

    class Element {
        int mask;
        String content;

        Element(int mask, String content) {
            this.mask = mask;
            this.content = content;
        }

        boolean isOpen() {
            return (mask & OPEN) != 0;
        }

        boolean isClose() {
            return (mask & CLOSE) != 0;
        }

        boolean isTitle() {
            return (mask & TITLE) != 0;
        }

        boolean isPoem() {
            return (mask & POEM) != 0;
        }

        boolean isEpigraph() {
            return (mask & EPIGRAPH) != 0;
        }

        boolean isCite() {
            return (mask & CITE) != 0;
        }

        boolean isTextAuthor() {
            return (mask & TEXT_AUTHOR) != 0;
        }
    }
}