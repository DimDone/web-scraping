package ru.webapp.serviceapp2;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkResponseItem;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class BulkJsonIndexer {
    private final ElasticsearchClient client;
    private final String indexName;
    private final ObjectMapper mapper = new ObjectMapper();
    private final AtomicInteger counter = new AtomicInteger(0);

    public BulkJsonIndexer(ElasticsearchClient client, String indexName) {
        this.client = client;
        this.indexName = indexName;
    }

    public void indexFile(Path file) {
        try {
            // Чтение и парсинг JSON
            DocumentStructure article = mapper.readValue(file.toFile(), DocumentStructure.class);

            // Индексация документа
            BulkResponse response = client.bulk(b -> b
                    .operations(op -> op
                            .index(idx -> idx
                                    .index(indexName)
                                    .id(article.getPageUrl()) // Используем URL как ID
                                    .document(article)
                            )
                    )
            );

            // Проверка ошибок
            for (BulkResponseItem item : response.items()) {
                if (item.error() != null) {
                    System.err.printf("Ошибка в файле %s: %s%n",
                            file.getFileName(), item.error().reason());
                }
            }

            // Прогресс-индикатор
            int processed = counter.incrementAndGet();
            if (processed % 100 == 0) {
                System.out.printf("Обработано %d документов%n", processed);
            }

        } catch (Exception e) {
            System.err.printf("Ошибка обработки файла %s: %s%n",
                    file.getFileName(), e.getMessage());
        }
    }

    public int getProcessedCount() {
        return counter.get();
    }
}
