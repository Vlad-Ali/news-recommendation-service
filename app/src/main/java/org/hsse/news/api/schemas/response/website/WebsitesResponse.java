package org.hsse.news.api.schemas.response.website;

import io.swagger.v3.oas.annotations.media.Schema;
import org.hsse.news.api.schemas.shared.WebsiteInfo;

import java.util.List;

@Schema(name = "WebsitesResponse", description = "Данные о выбранных сайтах пользователем")
public record WebsitesResponse(
    @Schema(description = "Выбранные сайты") List<WebsiteInfo> subscribed,
    @Schema(description = "Другие сайты") List<WebsiteInfo> other) {}
