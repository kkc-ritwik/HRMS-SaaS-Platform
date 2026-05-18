package com.hrms.search.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.hrms.search.config.SearchProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService {

    private final ElasticsearchClient es;
    private final SearchProperties props;

    public String indexName(String tenantId, String entity) {
        return props.getIndexPrefix() + "-" + entity.toLowerCase() + "-" +
                (tenantId == null ? "shared" : tenantId.toLowerCase());
    }

    public void index(String tenantId, String entity, String id, Object doc) {
        if (!props.isEnabled()) return;
        try {
            es.index(i -> i.index(indexName(tenantId, entity)).id(id).document(doc));
        } catch (IOException e) {
            log.warn("ES index failed: {}", e.getMessage());
        }
    }

    public void delete(String tenantId, String entity, String id) {
        if (!props.isEnabled()) return;
        try { es.delete(d -> d.index(indexName(tenantId, entity)).id(id)); }
        catch (IOException e) { log.warn("ES delete failed: {}", e.getMessage()); }
    }

    public List<Map<String, Object>> search(String tenantId, String entity, String q, int size) {
        if (!props.isEnabled()) return List.of();
        try {
            Query query = Query.of(b -> b.multiMatch(m -> m.query(q).fuzziness("AUTO")));
            SearchResponse<Map> resp = es.search(s -> s.index(indexName(tenantId, entity))
                    .query(query).size(size), Map.class);
            return resp.hits().hits().stream().map(Hit::source).map(o -> (Map<String, Object>) o).toList();
        } catch (Exception e) {
            log.warn("ES search failed: {}", e.getMessage());
            return List.of();
        }
    }
}
