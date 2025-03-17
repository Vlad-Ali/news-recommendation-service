package org.hsse.news.database.topic;

import org.hsse.news.database.topic.models.TopicId;
import org.springframework.stereotype.Service;

@Service
public class TopicService {
    public String getTopicNameById(final TopicId id) {
        return id.toString();
    }
}
