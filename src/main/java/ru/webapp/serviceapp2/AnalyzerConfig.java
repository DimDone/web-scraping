package ru.webapp.serviceapp2;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.indices.CreateIndexResponse;

import java.io.IOException;
import java.io.StringReader;

public class AnalyzerConfig {
    private final ElasticsearchClient elasticsearchClient;

    public AnalyzerConfig(ElasticsearchClient elasticsearchClient) {
        this.elasticsearchClient = elasticsearchClient;
    }

    public void configureIndex(String indexName) throws IOException {
        boolean indexExists = elasticsearchClient.indices().exists(e -> e.index(indexName)).value();

        if (indexExists) {
            System.out.println("Index '" + indexName + "' already exists.  Skipping creation.");
            return;
        }

    String settingsJson = """
    {
      "analysis": {
        "analyzer": {
          "my_russian": {
            "type": "custom",
            "tokenizer": "standard",
            "filter": [
              "lowercase",
              "russian_stop",
              "russian_morphology",
              "my_synonyms"
            ]
          },
          "fuzzy_analyzer": {
            "type": "custom",
            "tokenizer": "standard",
            "filter": [
              "lowercase",
              "russian_stop",
              "my_synonyms"
            ]
          }
        },
        "filter": {
          "russian_stop": {
            "type": "stop",
            "stopwords": "_russian_"
          },
          "russian_morphology": {
            "type": "stemmer",
            "language": "russian"
          },
          "my_synonyms": {
            "type": "synonym",
            "synonyms_path": "synonyms.txt",
            "expand": true,
            "lenient": true
          }
        }
      }
    }
    """;

    String mappingJson = """
    {
      "properties": {
        "pageUrl": { "type": "keyword" },
        "pageTitle": {
          "type": "text",
          "analyzer": "my_russian",
          "fields": {
            "keyword": { "type": "keyword" },
            "fuzzy": {
              "type": "text",
              "analyzer": "fuzzy_analyzer",
              "search_analyzer": "standard"
            }
          }
        },
        "pageContent": {
          "type": "text",
          "analyzer": "my_russian",
          "fields": {
            "fuzzy": {
              "type": "text",
              "analyzer": "fuzzy_analyzer",
              "search_analyzer": "standard"
            }
          }
        },
        "imageUrls": { "type": "keyword" }
      }
    }
    """;

        CreateIndexResponse createIndexResponse = elasticsearchClient.indices().create(c -> c
                .index(indexName)
                .settings(s -> s
                        .withJson(new StringReader(settingsJson)))  // Передаем StringReader напрямую
                .mappings(m -> m
                        .withJson(new StringReader(mappingJson)))   // Аналогично для маппинга
        );

        System.out.println("Index '" + indexName + "' created successfully: " + createIndexResponse.acknowledged());
    }

    public void deleteIndex(String indexName) throws IOException {
        if (elasticsearchClient.indices().exists(e -> e.index(indexName)).value()) {
            elasticsearchClient.indices().delete(e -> e.index(indexName));
            System.out.println("Index '" + indexName + "' deleted successfully.");
        }
    }

}
