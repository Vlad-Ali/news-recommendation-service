package org.hsse.news.api.schemas.request.website;

import org.hsse.news.api.schemas.shared.TopicInfo;
import org.hsse.news.api.schemas.shared.WebsiteInfo;

import java.util.List;

public record RecommendedWebsitesByTopic(List<WebsiteInfo> websiteInfos, TopicInfo topicInfo) {}
