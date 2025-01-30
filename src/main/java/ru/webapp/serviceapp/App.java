package ru.webapp.serviceapp;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;

public class App
{
    private static final int pagesCount = 500;

    public static void writeUrlsToFile(String filename, Set<String> urls) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(filename));

        for (String url : urls) {
            writer.write(url);
            writer.newLine();
        }

        writer.close();
    }
    public static void main( String[] args ) throws Exception
    {


//        UrlScraper.startScraping(pagesCount);
//        Set<String> urls = UrlScraper.getUniqueUrls();
//        writeUrlsToFile("urls.txt", urls);


    }
}
