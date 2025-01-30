package ru.webapp.serviceapp;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UrlScraper implements Runnable {
    private static final String LAND_OF_GAMES = "https://landofgames.ru/engine/ajax/controller.php?mod=blockpro";
    private static final String PAGE_NUM = "&pageNum=";
    private static final String BLOCK_ID = "&blockId=bpa_3788584818";
    private static final String THIS_URL = "&thisUrl=%2Farticles%2Freviews%2F";
    private static final Set<String> uniqueUrls = Collections.synchronizedSet(new HashSet<>());

    private int page;
    private String url;

    public UrlScraper(int page) {
        this.page = page;
        this.url = LAND_OF_GAMES + PAGE_NUM + page + BLOCK_ID + THIS_URL;
    }

    @Override
        public void run(){
            try {
                Document document = Jsoup.connect(this.url).get();
                Elements links = document.select("div.item-content a[href]");

                for (Element link : links) {
                    String absUrl = link.absUrl("href");
                    if (absUrl.endsWith(".html")) {
                        uniqueUrls.add(absUrl);
                    }
                }
                Thread.sleep(1000);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
    }

    public static Set<String> getUniqueUrls() {
        return uniqueUrls;
    }

    public static void startScraping(int pagesCount) throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(10);

        for (int i = 1; i <= pagesCount; i++) {
            executorService.submit(new UrlScraper(i));
        }
        executorService.shutdown();
        while (!executorService.isTerminated()) {
            // Ждём завершения всех потоков
        }
    }
}
