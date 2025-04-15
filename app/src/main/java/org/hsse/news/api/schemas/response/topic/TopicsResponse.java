package org.hsse.news.api.schemas.response.topic;

import io.swagger.v3.oas.annotations.media.Schema;
import org.hsse.news.api.schemas.shared.TopicInfo;

import java.util.List;

@Schema(name = "TopicsResponse", description = "Информация о выбранных темах пользователя")
public record TopicsResponse(
        @Schema(description = "Выбранные темы") List<TopicInfo> subscribed,
        @Schema(description = "Другие темы") List<TopicInfo> other
        ) {}
