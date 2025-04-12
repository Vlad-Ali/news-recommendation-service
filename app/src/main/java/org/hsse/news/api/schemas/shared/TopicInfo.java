package org.hsse.news.api.schemas.shared;

import io.swagger.v3.oas.annotations.media.Schema;
import org.jetbrains.annotations.NotNull;

@Schema(name = "TopicInfo", description = "Информация о теме без ID создателя")
public record TopicInfo(
        @Schema(description = "ID темы") @NotNull Long topicID,
        @Schema(description = "Описание темы") @NotNull String description
) {
}
