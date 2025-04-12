package ru.webapp.serviceapp2;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
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
            Query exactTermQuery = TermQuery.of(t -> t
                    .field("pageTitle.keyword")
                    .value(query)
                    .boost(15.0f) // Самый высокий приоритет для точного совпадения
            )._toQuery();

            Query exactPhraseQuery = MatchPhraseQuery.of(m -> m
                    .field("pageTitle")
                    .query(query)
                    .boost(10.0f) // Высокий приоритет для точной фразы в заголовке
                    .slop(2) // Допускает 2 слова между терминами
            )._toQuery();

            Query titleMatchQuery = MatchQuery.of(m -> m
                    .field("pageTitle")
                    .query(query)
                    .boost(8.0f)
                    .operator(Operator.And) // Все слова должны быть в заголовке
            )._toQuery();

            Query contentMatchQuery = MatchQuery.of(m -> m
                    .field("pageContent")
                    .query(query)
                    .boost(3.0f)
                    .operator(Operator.Or) // Хотя бы одно слово в контенте
            )._toQuery();

            Query fuzzyQuery = MultiMatchQuery.of(m -> m
                    .fields("pageTitle^3", "pageContent")
                    .query(query)
                    .fuzziness("AUTO")
                    .prefixLength(2) // Первые 2 символа должны точно совпадать
                    .boost(2.0f) // Ниже приоритет для fuzzy-результатов
            )._toQuery();

            // 2. Собираем итоговый запрос
            SearchResponse<Map> response = elasticsearchClient.search(s -> s
                            .index("game_articles")
                            .query(q -> q
                                    .bool(b -> b
                                            .should(
                                                    exactTermQuery,
                                                    exactPhraseQuery,
                                                    titleMatchQuery,
                                                    contentMatchQuery,
                                                    fuzzyQuery
                                            )
                                            .minimumShouldMatch("2")
                                    )
                            )
                            .sort(so -> so
                                    .score(s1 -> s1.order(SortOrder.Desc))
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
