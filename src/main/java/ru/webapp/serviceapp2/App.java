package ru.webapp.serviceapp2;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

public class App {
    public static void main(String[] args) throws IOException {
        // 1. Инициализация клиента
        RestClient restClient = RestClient.builder(
                new HttpHost("localhost", 9200)).build();

        ElasticsearchTransport transport = new RestClientTransport(
                restClient, new JacksonJsonpMapper());

        ElasticsearchClient client = new ElasticsearchClient(transport);

//        new AnalyzerConfig(client).deleteIndex("game_articles");
        // 2. Создание индекса (если нужно)
        try {
            new AnalyzerConfig(client).configureIndex("game_articles");
        } catch (Exception e) {
            System.err.println("Ошибка создания индекса: " + e.getMessage());
            return;
        }

        // 3. Индексация документов
        BulkJsonIndexer indexer = new BulkJsonIndexer(client, "game_articles");
        Path jsonDir = Paths.get("D:\\4 course\\Scraper\\json_data");

        try {
            System.out.println("Начало индексации...");

            Files.walk(jsonDir)
                    .filter(path -> path.toString().endsWith(".json"))
                    .sorted(Comparator.comparing(Path::toString))
                    .forEach(indexer::indexFile);

            System.out.printf("Индексация завершена! Обработано %d документов%n",
                    indexer.getProcessedCount());

        } catch (IOException e) {
            System.err.println("Ошибка чтения файлов: " + e.getMessage());
        }

        new ElasticsearchSearcher(client).startSearchingLoop();
    }
}
