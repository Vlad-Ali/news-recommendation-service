package org.hsse.news.api.schemas.request.website;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(name = "SubWebsitesUpdateRequest", description = "Данные для обновления выбранных сайтов")
public record SubWebsitesUpdateRequest(
    @Schema(description = "Список ID выбранных сайтов для обновления") List<Long> websiteIds) {}
