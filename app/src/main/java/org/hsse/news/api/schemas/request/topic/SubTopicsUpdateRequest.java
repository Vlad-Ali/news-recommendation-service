package org.hsse.news.api.schemas.request.topic;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(name = "SubTopicsUpdateRequest", description = "Данные для обновления выбранных тем")
public record SubTopicsUpdateRequest(@Schema(description = "Список ID выбранных тем для обновления") List<Long> topicIds) {
}
