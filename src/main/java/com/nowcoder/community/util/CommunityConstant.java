package com.nowcoder.community.util;

public interface CommunityConstant {
    int ACTIVATION_SUCCESS = 0;
    int ACTIVATION_REPEAT = 1;
    int ACTIVATION_FAILURE = 2;

    // Default expiration time
    int DEFAULT_EXPIRED_SECONDS = 3600 * 12;

    // Recording expiration time
    int RECORD_EXPIRED_SECOND =  3600 * 24 * 10;

    // Entity: post
    int ENTITY_TYPE_POST = 1;

    // Entity: comment
    int ENTITY_TYPE_COMMENT = 2;

    // Entity: user
    int ENTITY_TYPE_USER = 3;

    // Topic: comment
    String TOPIC_COMMENT = "comment";

    // Topic: like
    String TOPIC_LIKE = "like";

    // Topic: follow
    String TOPIC_FOLLOW = "follow";

    // Topic: publish
    String TOPIC_PUBLISH = "publish";

    int SYSTEM_USER_ID = 1;

}
