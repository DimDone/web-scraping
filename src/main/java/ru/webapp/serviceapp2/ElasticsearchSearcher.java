package ru.webapp.serviceapp2;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;

import java.util.Map;
import java.util.Scanner;

public class ElasticsearchSearcher {
    private final ElasticsearchClient elasticsearchClient;
    private final Scanner scanner;

    public ElasticsearchSearcher(ElasticsearchClient elasticsearchClient) {
        this.elasticsearchClient = elasticsearchClient;
        this.scanner = new Scanner(System.in);
    }

    public void startSearchingLoop() {
        System.out.println("=== Поиск по индексу (для выхода нажмите Ctrl+C) ===");

        while (true) {
            System.out.print("\nПоисковый запрос: ");
            String query = scanner.nextLine().trim();

            if (query.equalsIgnoreCase("exit")) {
                break;
            }

            searchAndPrintResults(query);
        }
    }

    private void searchAndPrintResults(String query) {
        try {
            SearchResponse<Map> response = elasticsearchClient.search(s -> s
                            .index("game_articles")
                            .query(q -> q
                                    .multiMatch(m -> m
                                            .fields("pageTitle", "pageContent")
                                            .fuzziness("AUTO")
                                            .query(query))
                            )
                            .size(10),
                    Map.class
            );

            System.out.printf("\nНайдено: %d | Показано: %d%n",
                    response.hits().total().value(),
                    response.hits().hits().size());

            for (Hit<Map> hit : response.hits().hits()) {
                System.out.printf("\nID: %s\nScore: %.2f\nДокумент: %s%n",
                        hit.id(),
                        hit.score(),
                        hit.source());
            }

        } catch (Exception e) {
            System.err.println("Ошибка поиска: " + e.getMessage());
        }
    }
}
