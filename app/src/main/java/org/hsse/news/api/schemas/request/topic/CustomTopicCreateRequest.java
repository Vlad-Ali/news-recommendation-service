package org.hsse.news.api.schemas.request.topic;

import io.swagger.v3.oas.annotations.media.Schema;
import org.jetbrains.annotations.NotNull;

@Schema(name = "CustomTopicCreateRequest", description = "Данные для создания темы")
public record CustomTopicCreateRequest(@Schema(description = "Название темы") @NotNull String name) {}
