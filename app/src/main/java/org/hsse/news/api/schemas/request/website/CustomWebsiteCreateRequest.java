package org.hsse.news.api.schemas.request.website;

import io.swagger.v3.oas.annotations.media.Schema;
import org.jetbrains.annotations.NotNull;

@Schema(name = "CustomWebsiteCreateRequest", description = "Данные для создания сайта")
public record CustomWebsiteCreateRequest(
    @Schema(description = "URL сайта") @NotNull String url,
    @Schema(description = "Описание сайта") @NotNull String description) {}
