package com.example.alexey.myapplication;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import java.io.InputStream;
import java.lang.Exception;

public class ArticleParser {
    private DocumentBuilder builder;

    public ArticleParser() {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            builder = factory.newDocumentBuilder();
        } catch (Exception ex) {}
    }

    public Article parse(InputStream input) {
        Article article = null;
        try {
            Document document = builder.parse(input);
            NodeList text = document.getElementsByTagName("text");
            if (text.getLength() > 1) {
                article = new Article();
                article.setWord(text.item(0).getTextContent());
                article.addVariant(text.item(1).getTextContent());
                for (int i = 2; i < text.getLength(); i++) {
                    Node node = text.item(i);
                    Node parent = node.getParentNode();
                    if (parent.getNodeName().equals("syn")) {
                        article.addVariant(node.getTextContent());
                    }
                }
            }
        } catch (Exception ex) {}
        return article;
    }
}