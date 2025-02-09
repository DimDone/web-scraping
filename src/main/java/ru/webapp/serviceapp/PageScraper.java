package ru.webapp.serviceapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PageScraper implements Runnable {
    private static final String OUTPUT_DIR = "json_data";
    private static final ObjectMapper mapper = new ObjectMapper();

    private final int id;
    private final String pageUrl;

    public PageScraper(String pageUrl, int id) {
        this.id = id;
        this.pageUrl = pageUrl;
    }

    @Override
    public void run() {
        Document document = null;
        try {
            document = Jsoup.connect(this.pageUrl).get();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Elements textElements = document.select("div#main_art p");
        List<String> text = new ArrayList<String>();
        for (Element element : textElements) {
            text.add(element.text());
        }

        Elements links = document.select("figure.image_case a.img_link");
        List<String> imgUrls = new ArrayList<>();
        for (Element link : links) {
            String imgUrl = link.absUrl("href");
            imgUrls.add(imgUrl);
        }

        try {
            savePageData(new PageData(this.pageUrl, document.title(), String.join(" ", text), imgUrls), this.id);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public String getPageUrl() {
        return pageUrl;
    }

    public static void savePageData(PageData pageData, int pageNumber) throws IOException {
        Path outputDir = Paths.get(OUTPUT_DIR);
        String fileName = "JSON_page_" + pageNumber + ".json";
        Path outputFile = outputDir.resolve(fileName);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile.toFile()))){
            String json = mapper.writeValueAsString(pageData);
            writer.write(json);
        }
    }

    public static void startScraping(String readingFilePath) throws IOException {
        ExecutorService executorService = Executors.newFixedThreadPool(30);

        int pageNumber = 1;
        Path path = Paths.get(readingFilePath);
        try (BufferedReader br = new BufferedReader(new FileReader(path.toFile()))) {
            String line;
            while ((line = br.readLine()) != null) {
                executorService.submit(new PageScraper(line, pageNumber));
                pageNumber++;
            }
            executorService.shutdown();

            while (!executorService.isTerminated()) {

            }
        }
    }
}
