package com.nowcoder.community.service;

import com.nowcoder.community.dao.MessageMapper;
import com.nowcoder.community.entity.Message;
import com.nowcoder.community.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class MessageService {

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    public List<Message> findConversations (int userId, int offset, int limit) {
        return messageMapper.selectConversations(userId, offset, limit);
    }

    public int findConversationCount (int userId) {
        return messageMapper.selectConversationCount(userId);
    }

    public List<Message> findDM (String conversationId, int offset, int limit) {
        return messageMapper.selectDM(conversationId, offset, limit);
    }

    public int findDMCount (String conversationId) {
        return messageMapper.selectDMCount(conversationId);
    }

    public int findUnreadCount (int userId, String conversationId) {
        return messageMapper.selectUnreadCount(userId, conversationId);
    }

    public int addMessage (Message message) {
        message.setContent(HtmlUtils.htmlEscape(message.getContent()));
        message.setContent(sensitiveFilter.filter(message.getContent()));
        return messageMapper.insertMessage(message);
    }

    public int readMessage (List<Integer> ids) {
        return messageMapper.updateStatus(ids,  1);
    }

    public Message findLastNotice(int userId, String topic) {
        return messageMapper.selectLastNotice(userId, topic);
    }

    public int findNoticeCount(int userId, String topic) {
        return messageMapper.selectNoticeCount(userId, topic);
    }

    public int findNoticeUnreadCount(int userId, String topic) {
        return messageMapper.selectNoticeUnreadCount(userId, topic);
    }

    public List<Message> findNotices(int userId, String topic, int offset, int limit) {
        return messageMapper.selectNotices(userId, topic, offset, limit);
    }
}
