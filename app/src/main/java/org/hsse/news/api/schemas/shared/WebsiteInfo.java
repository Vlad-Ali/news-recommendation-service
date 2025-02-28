package org.hsse.news.api.schemas.shared;

import io.swagger.v3.oas.annotations.media.Schema;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;
import org.jetbrains.annotations.NotNull;

@Schema(name = "WebsiteInfo", description = "Информация о сайте без ID создателя")
public record WebsiteInfo(
    @Schema(description = "ID сайта") @NotNull Long websiteId,
    @Schema(description = "URL сайта") @NotNull String url,
    @Schema(description = "Описание сайта") @NotNull String description) {
  @JdbiConstructor
  public WebsiteInfo {}
}
