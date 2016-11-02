package com.rltx.parser;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class XmlDocumentParser {

    public static List<Document> parse(String path) throws DocumentException {
        List<Document> documents = new ArrayList<>();
        File file = new File(path);
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File file2: files) {
                Document document = getDocument(file2.getAbsolutePath());
                documents.add(document);
            }
        } else {
            Document document = getDocument(path);
            documents.add(document);
        }
        return documents;
    }

    private static Document getDocument(String filePath) throws DocumentException {
        SAXReader reader = new SAXReader();
        return reader.read(filePath);
    }

}
