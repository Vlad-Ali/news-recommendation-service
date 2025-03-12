package org.hsse.news.api.controllers.website;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.hsse.news.api.schemas.request.website.CustomWebsiteCreateRequest;
import org.hsse.news.api.schemas.request.website.SubWebsitesUpdateRequest;
import org.hsse.news.api.schemas.response.website.WebsitesResponse;
import org.hsse.news.api.schemas.shared.WebsiteInfo;
import org.hsse.news.database.website.exceptions.QuantityLimitExceededWebsitesPerUserException;
import org.hsse.news.database.website.exceptions.WebsiteAlreadyExistsException;
import org.hsse.news.database.website.exceptions.WebsiteNotFoundException;
import org.hsse.news.database.website.exceptions.WebsiteRSSNotValidException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/default")
@Tag(name = "Website API", description = "Управление сайтами для пользователей")
public interface WebsiteOperations {
    @Operation(summary = "Найти сайт по ID")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Сайт найден"), @ApiResponse(responseCode = "400", description = "Такой сайт не найден")}) // NOPMD - suppressed AvoidDuplicateLiterals - irrelevant
    @GetMapping("/{id}")
    ResponseEntity<WebsiteInfo> get(
            @Parameter(description = "ID сайта для получения") @PathVariable Long id) throws WebsiteNotFoundException;

    @Operation(summary = "Получить все выбранные пользователем сайты")
    @ApiResponse(responseCode = "200", description = "Сайты получены") // NOPMD - suppressed AvoidDuplicateLiterals - irrelevant
    @GetMapping("/user")
    ResponseEntity<WebsitesResponse> getUsersWebsites();

    @Operation(summary = "Установить выбранные сайты для пользователя")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Выбранные сайты обновлены"), @ApiResponse(responseCode = "411", description = "Превышен лимит выбранных сайтов"),@ApiResponse(responseCode = "400", description = "Такой сайт не найден")}) // NOPMD - suppressed AvoidDuplicateLiterals - irrelevant
    @PatchMapping
    ResponseEntity<String> updateSubWebsites(
            @Parameter(description = "Данные для обновления выбранных сайтов") @RequestBody
            SubWebsitesUpdateRequest subWebsitesUpdateRequest) throws QuantityLimitExceededWebsitesPerUserException, WebsiteNotFoundException;

    @Operation(summary = "Создать сайт")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Сайт создан"), @ApiResponse(responseCode = "409", description = "Такой сайт уже создан"), @ApiResponse(responseCode = "415", description = "Неправильный RSS")}) // NOPMD - suppressed AvoidDuplicateLiterals - irrelevant
    @PostMapping("/custom")
    ResponseEntity<WebsiteInfo> create(
            @Parameter(description = "Данные для создания Сайта пользователем") @RequestBody
            CustomWebsiteCreateRequest customWebsiteCreateRequest) throws WebsiteAlreadyExistsException, WebsiteRSSNotValidException;

    @Operation(summary = "Удалить созданный пользователем сайт")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Сайт удален"), @ApiResponse(responseCode = "400", description = "Такой сайт не найден")}) // NOPMD - suppressed AvoidDuplicateLiterals - irrelevant
    @DeleteMapping("/custom/{websiteId}")
    ResponseEntity<String> delete(
            @Parameter(description = "ID созданного сайта для удаления") @PathVariable Long websiteId) throws WebsiteNotFoundException;


}

