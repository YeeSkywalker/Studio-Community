package com.nowcoder.community.controller;

import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.service.ElasticsearchService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ElasticsearchController implements CommunityConstant {
    @Autowired
    private ElasticsearchService elasticsearchService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    @RequestMapping(path = "/search", method = RequestMethod.GET)
    public String search(String keyword, Page page, Model model) {
        Map<String, Object> map = elasticsearchService.searchDiscussPost(keyword, page.getCurrent() - 1, page.getLimit());
        List<DiscussPost> searchRes = (List<DiscussPost>) map.get("discussPosts");
        List<Map<String, Object>> discussPosts = new ArrayList<>();
        if (!searchRes.isEmpty()) {
            for (DiscussPost post : searchRes) {
                Map<String, Object> postMap = new HashMap<>();
                postMap.put("post", post);
                postMap.put("user", userService.findUserById(post.getUserId()));
                postMap.put("likeCount", likeService.findLikeCount(ENTITY_TYPE_POST, post.getId()));
                discussPosts.add(postMap);
            }
        }
        model.addAttribute("discussPosts", discussPosts);
        model.addAttribute("keyword", keyword);

        page.setPath("/search?keyword=" + keyword);
        page.setRows(searchRes.isEmpty()? 0 : ((Long) map.get("totalHits")).intValue());
        return "/site/search";
    }
}
