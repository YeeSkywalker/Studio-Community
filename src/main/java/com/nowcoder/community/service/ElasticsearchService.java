package com.nowcoder.community.service;

import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import com.nowcoder.community.dao.elasticsearch.DiscussPostRepository;
import com.nowcoder.community.entity.DiscussPost;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.HighlightQuery;
import org.springframework.data.elasticsearch.core.query.highlight.Highlight;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightField;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightParameters;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ElasticsearchService {
    @Autowired
    private DiscussPostRepository discussPostRepository;

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    public void saveDiscussPost(DiscussPost discussPost) {
        discussPostRepository.save(discussPost);
    }

    public void deleteDiscussPost(int id) {
        discussPostRepository.deleteById(id);
    }

    public Map<String, Object> searchDiscussPost(String keyword, int current, int limit) {
        List<HighlightField> fields = new ArrayList<>();
        fields.add(new HighlightField("title"));
        fields.add(new HighlightField("content"));
        NativeQuery query = new NativeQueryBuilder()
                .withQuery(QueryBuilders.queryString(fn -> fn.query(keyword).fields("title", "content")))
                .withSort(SortOptions.of(fn->fn.field(q->q.field("type").order(SortOrder.Desc))))
                .withSort(SortOptions.of(fn->fn.field(q->q.field("score").order(SortOrder.Desc))))
                .withSort(SortOptions.of(fn->fn.field(q->q.field("createTime").order(SortOrder.Desc))))
                .withPageable(PageRequest.of(current, limit))
                .withHighlightQuery(new HighlightQuery(
                        new Highlight(new HighlightParameters.HighlightParametersBuilder()
                                .withPreTags("<em>")
                                .withPostTags("</em>")
                                .build(), fields), DiscussPost.class))
                .build();
        Map<String, Object> map = new HashMap<>();
        SearchHits<DiscussPost> searchHits = elasticsearchOperations.search(query, DiscussPost.class);
        map.put("totalHits", searchHits.getTotalHits());

        List<DiscussPost> discussPosts = new ArrayList<>();
        for (SearchHit<DiscussPost> searchHit : searchHits) {
            DiscussPost post = searchHit.getContent();
            List<String> titleHighlightFields = searchHit.getHighlightField("title");
            if (!titleHighlightFields.isEmpty()) {
                post.setTitle(titleHighlightFields.get(0));
            }

            List<String> contentHighlightFields = searchHit.getHighlightField("content");
            if (!contentHighlightFields.isEmpty()) {
                post.setContent(contentHighlightFields.get(0));
            }
            discussPosts.add(post);
        }
        map.put("discussPosts", discussPosts);
        return map;
    }
}
