package com.nowcoder.community;

import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.dao.elasticsearch.DiscussPostRepository;
import com.nowcoder.community.entity.DiscussPost;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.*;
import org.springframework.data.elasticsearch.core.query.HighlightQuery;
import org.springframework.data.elasticsearch.core.query.highlight.Highlight;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightField;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightParameters;
import org.springframework.test.context.ContextConfiguration;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class ElasticSearchTests {
    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private DiscussPostRepository discussPostRepository;

    @Autowired
    private ElasticsearchOperations operations;

    @Test
    public void testInsert() {
        discussPostRepository.save(discussPostMapper.selectDiscussPostById(241));
        discussPostRepository.save(discussPostMapper.selectDiscussPostById(242));
        discussPostRepository.save(discussPostMapper.selectDiscussPostById(243));
    }

    @Test
    public void testInsertMulti() {
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(101, 0, 100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(102, 0, 100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(103, 0, 100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(111, 0, 100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(112, 0, 100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(131, 0, 100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(132, 0, 100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(133, 0, 100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(134, 0, 100));
    }

    @Test
    public void testUpdate() {
        DiscussPost discussPost = discussPostMapper.selectDiscussPostById(231);
        discussPost.setContent("测试es");
        discussPostRepository.save(discussPost);

    }

    @Test
    public void testDelete() {
        //discussPostRepository.deleteById(231);
        discussPostRepository.deleteAll();
    }

    @Test
    public void testSearchByRepository() {
        List<HighlightField> fields = new ArrayList<>();
        fields.add(new HighlightField("title"));
        fields.add(new HighlightField("content"));
        NativeQuery query = new NativeQueryBuilder()
                .withQuery(QueryBuilders.queryString(fn -> fn.query("互联网寒冬").fields("title", "content")))
                .withSort(SortOptions.of(fn->fn.field(q->q.field("type").order(SortOrder.Desc))))
                .withSort(SortOptions.of(fn->fn.field(q->q.field("score").order(SortOrder.Desc))))
                .withSort(SortOptions.of(fn->fn.field(q->q.field("createTime").order(SortOrder.Desc))))
                .withPageable(PageRequest.of(0, 10))
                .withHighlightQuery(new HighlightQuery(
                        new Highlight(new HighlightParameters.HighlightParametersBuilder()
                                .withPreTags("<em>")
                                .withPostTags("</em>")
                                .build(), fields), DiscussPost.class))
                .build();
        SearchHits<DiscussPost> searchHits = operations.search(query, DiscussPost.class);
        System.out.println("Have " + searchHits.getTotalHits()+ " data in total");

        SearchPage<DiscussPost> page = SearchHitSupport.searchPageFor(searchHits, query.getPageable());
        System.out.println("Current page is " + page.getNumber());
        System.out.println("Current page has " + page.getNumberOfElements() + " data");
        System.out.println("We have " + page.getTotalPages() + " pages in total");

        for (SearchHit<DiscussPost> searchHit : page) {
            System.out.println(searchHit.getContent());
        }

        for (SearchHit<DiscussPost> searchHit : page) {
            List<String> title = searchHit.getHighlightField("title");
            List<String> content = searchHit.getHighlightField("content");

            searchHit.getContent().setTitle(String.join("", title));
            searchHit.getContent().setContent(String.join("", content));
        }

        for (SearchHit<DiscussPost> searchHit : page) {
            System.out.println(searchHit.getContent());
        }
    }
}
