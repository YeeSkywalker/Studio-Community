package com.nowcoder.community.controller;

import com.alibaba.fastjson.JSONObject;
import com.nowcoder.community.entity.Message;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.MessageService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.HtmlUtils;

import java.util.*;

@Controller
public class MessageController implements CommunityConstant {

    @Autowired
    private MessageService messageService;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @RequestMapping(path = "/dm/list", method = RequestMethod.GET)
    public String getDMList(Model model, Page page) {
        User user = hostHolder.getUser();
        // Set up pagination
        page.setLimit(5);
        page.setPath("/dm/list");
        page.setRows(messageService.findConversationCount(user.getId()));

        // Create conversation list
        List<Message> conversationList = messageService.findConversations(
                user.getId(), page.getOffset(), page.getLimit()
        );

        List<Map<String, Object>> conversations = new ArrayList<>();

        if (conversationList != null) {
            for (Message message : conversationList) {
                Map<String, Object> map = new HashMap<>();
                map.put("conversation", message);
                map.put("dmCount", messageService.findDMCount(message.getConversationId()));
                map.put("unreadCount", messageService.findUnreadCount(user.getId(), message.getConversationId()));
                int targetId = message.getFromId() == user.getId() ? message.getToId() : message.getFromId();
                map.put("target", userService.findUserById(targetId));

                conversations.add(map);
            }
        }

        model.addAttribute("conversations", conversations);

        int totalUnreadDmCount = messageService.findUnreadCount(user.getId(), null);
        model.addAttribute("totalUnreadDmCount", totalUnreadDmCount);

        int totalUnreadNoticeCount = messageService.findNoticeUnreadCount(user.getId(), null);
        model.addAttribute("totalUnreadNoticeCount", totalUnreadNoticeCount);

        return "/site/dm";
    }


    @RequestMapping(path = "/dm/detail/{conversationId}", method = RequestMethod.GET)
    public String getDmDetail(@PathVariable("conversationId") String conversationId, Page page, Model model) {
        // Set up pagination
        page.setLimit(5);
        page.setPath("/dm/detail/" + conversationId);
        page.setRows(messageService.findDMCount(conversationId));

        List<Message> dmList = messageService.findDM(conversationId, page.getOffset(), page.getLimit());
        List<Map<String, Object>> dms = new ArrayList<>();

        if (dmList != null) {
            for (Message message : dmList) {
                Map<String, Object> map = new HashMap<>();
                map.put("dm", message);
                map.put("fromUser", userService.findUserById(message.getFromId()));
                dms.add(map);
            }
        }

        model.addAttribute("dms", dms);
        model.addAttribute("target", getTarget(conversationId));
        List<Integer> ids = getDmIds(dmList);
        if (!ids.isEmpty()) {
            messageService.readMessage(ids);
        }
        return "/site/dm-detail";

    }

    private User getTarget(String conversationId) {
        String[] ids = conversationId.split("_");
        int id0 = Integer.parseInt(ids[0]);
        int id1 = Integer.parseInt(ids[1]);

        if (hostHolder.getUser().getId() == id0) {
            return userService.findUserById(id1);
        } else {
            return userService.findUserById(id0);
        }
    }

    private List<Integer> getDmIds(List<Message> dmList) {
        List<Integer> ids = new ArrayList<>();
        if (dmList != null) {
            for (Message message : dmList) {
                if (hostHolder.getUser().getId() == message.getToId() && message.getStatus() == 0) {
                    ids.add(message.getId());
                }
            }
        }
        return ids;
    }

    @RequestMapping(path = "/dm/send", method = RequestMethod.POST)
    @ResponseBody
    public String sendDm(String targetUsername, String content) {
        User target = userService.findUserByName(targetUsername);
        if (target == null) {
            return CommunityUtil.getJSONString(204, "User not found!");
        }

        Message message = new Message();
        message.setFromId(hostHolder.getUser().getId());
        message.setToId(target.getId());
        if (message.getFromId() < message.getToId()) {
            message.setConversationId(message.getFromId() + "_" + message.getToId());
        } else {
            message.setConversationId(message.getToId() + "_" + message.getFromId());
        }
        message.setContent(content);
        message.setCreateTime(new Date());

        messageService.addMessage(message);

        return CommunityUtil.getJSONString(200);
    }

    @RequestMapping(path = "/notice/list", method = RequestMethod.GET)
    public String getNoticeList(Model model) {
        User user = hostHolder.getUser();

        // Find comment notification
        Message comment = messageService.findLastNotice(user.getId(), TOPIC_COMMENT);
        if (comment != null) {
            Map<String, Object> commentVo = new HashMap<>();
            commentVo.put("message", comment);

            String content = HtmlUtils.htmlUnescape(comment.getContent());
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);

            commentVo.put("user", userService.findUserById((Integer) data.get("userId")));
            commentVo.put("entityType", data.get("entityType"));
            commentVo.put("entityId", data.get("entityId"));
            commentVo.put("postId", data.get("postId"));

            int count = messageService.findNoticeCount(user.getId(), TOPIC_COMMENT);
            commentVo.put("count", count);

            int unreadCount = messageService.findNoticeUnreadCount(user.getId(), TOPIC_COMMENT);
            commentVo.put("unreadCount", unreadCount);
            model.addAttribute("commentNotice", commentVo);
        }

        // Find like notification
        Message like = messageService.findLastNotice(user.getId(), TOPIC_LIKE);
        if (like != null) {
            Map<String, Object> likeVo = new HashMap<>();
            likeVo.put("message", like);

            String content = HtmlUtils.htmlUnescape(like.getContent());
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);

            likeVo.put("user", userService.findUserById((Integer) data.get("userId")));
            likeVo.put("entityType", data.get("entityType"));
            likeVo.put("entityId", data.get("entityId"));
            likeVo.put("postId", data.get("postId"));

            int count = messageService.findNoticeCount(user.getId(), TOPIC_LIKE);
            likeVo.put("count", count);

            int unreadCount = messageService.findNoticeUnreadCount(user.getId(), TOPIC_LIKE);
            likeVo.put("unreadCount", unreadCount);
            model.addAttribute("likeNotice", likeVo);
        }

        // Find follow notification
        Message follow = messageService.findLastNotice(user.getId(), TOPIC_FOLLOW);
        if (follow != null) {
            Map<String, Object> followVo = new HashMap<>();
            followVo.put("message", follow);

            String content = HtmlUtils.htmlUnescape(follow.getContent());
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);

            followVo.put("user", userService.findUserById((Integer) data.get("userId")));
            followVo.put("entityType", data.get("entityType"));
            followVo.put("entityId", data.get("entityId"));

            int count = messageService.findNoticeCount(user.getId(), TOPIC_FOLLOW);
            followVo.put("count", count);

            int unreadCount = messageService.findNoticeUnreadCount(user.getId(), TOPIC_FOLLOW);
            followVo.put("unreadCount", unreadCount);
            model.addAttribute("followNotice", followVo);
        }

        // Find unread count of dm and notice
        int totalUnreadDmCount = messageService.findUnreadCount(user.getId(), null);
        model.addAttribute("totalUnreadDmCount", totalUnreadDmCount);

        int totalUnreadNoticeCount = messageService.findNoticeUnreadCount(user.getId(), null);
        model.addAttribute("totalUnreadNoticeCount", totalUnreadNoticeCount);

        return "/site/notice";
    }

    @RequestMapping(path = "/notice/detail/{topic}", method = RequestMethod.GET)
    public String getNoticeDetail(@PathVariable("topic") String topic, Page page, Model model) {
        User user = hostHolder.getUser();

        page.setLimit(5);
        page.setPath("/notice/detail/" + topic);
        page.setRows(messageService.findNoticeCount(user.getId(), topic));

        List<Message> noticeList = messageService.findNotices(user.getId(), topic, page.getOffset(), page.getLimit());
        List<Map<String, Object>> noticeMapList = new ArrayList<>();

        if (noticeList != null) {
            for (Message notice : noticeList) {
                Map<String, Object> map = new HashMap<>();
                // Notice
                map.put("notice", notice);

                // Content
                String content = HtmlUtils.htmlUnescape(notice.getContent());
                Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);
                map.put("user", userService.findUserById((Integer) data.get("userId")));
                map.put("entityType", data.get("entityType"));
                map.put("entityId", data.get("entityId"));
                map.put("postId", data.get("postId"));

                // Notification from user
                map.put("fromUser", userService.findUserById(notice.getFromId()));

                noticeMapList.add(map);
            }
        }

        model.addAttribute("notices", noticeMapList);

        // Set read state
        List<Integer> ids = getDmIds(noticeList);
        if (!ids.isEmpty()) {
            messageService.readMessage(ids);
        }

        return "/site/notice-detail";
    }
}
