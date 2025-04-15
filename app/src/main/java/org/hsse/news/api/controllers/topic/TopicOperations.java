package org.hsse.news.api.controllers.topic;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.hsse.news.api.schemas.request.topic.CustomTopicCreateRequest;
import org.hsse.news.api.schemas.request.topic.SubTopicsUpdateRequest;
import org.hsse.news.api.schemas.response.topic.TopicsResponse;
import org.hsse.news.api.schemas.shared.TopicInfo;
import org.hsse.news.database.topic.exceptions.QuantityLimitExceededTopicsPerUserException;
import org.hsse.news.database.topic.exceptions.TopicAlreadyExistsException;
import org.hsse.news.database.topic.exceptions.TopicNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/default")
@Tag(name = "Topic API", description = "Управление тем для пользователей")
public interface TopicOperations {
    @Operation(summary = "Найти тему по ID")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Тема найдена"), @ApiResponse(responseCode = "400", description = "Такая тема не найдена")}) // NOPMD - suppressed AvoidDuplicateLiterals - irrelevant
    @GetMapping("/id")
    ResponseEntity<TopicInfo> get(@Parameter(description = "ID емы для получения") @PathVariable Long id) throws TopicNotFoundException;


    @Operation(summary = "Получить все выбранные пользователем темы")
    @ApiResponse(responseCode = "200", description = "Темы получены") // NOPMD - suppressed AvoidDuplicateLiterals - irrelevant
    @GetMapping("/user")
    ResponseEntity<TopicsResponse> getUsersTopics();

    @Operation(summary = "Установить выбранные темы для пользователя")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Выбранные темы обновлены"), @ApiResponse(responseCode = "411", description = "Превышен лимит выбранных тем"),@ApiResponse(responseCode = "400", description = "Такая тема не найдена")}) // NOPMD - suppressed AvoidDuplicateLiterals - irrelevant
    @PatchMapping
    ResponseEntity<String> updateSubTopics(@Parameter(description = "Данные для обновления выбранных тем") @RequestBody SubTopicsUpdateRequest subTopicsUpdateRequest) throws QuantityLimitExceededTopicsPerUserException, TopicNotFoundException;

    @Operation(summary = "Создать тему")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Тема создана"), @ApiResponse(responseCode = "409", description = "Такая тема уже создана")}) // NOPMD - suppressed AvoidDuplicateLiterals - irrelevant
    @PostMapping("/custom")
    ResponseEntity<TopicInfo> create(@Parameter(description = "Данные для создания темы пользователем") @RequestBody
                                     CustomTopicCreateRequest customTopicCreateRequest) throws TopicAlreadyExistsException;

    @Operation(summary = "Удалить созданную пользователем тему")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Тема удалена"), @ApiResponse(responseCode = "400", description = "Такая тема не найдена")}) // NOPMD - suppressed AvoidDuplicateLiterals - irrelevant
    @DeleteMapping("/custom/{topicId}")
    ResponseEntity<String> delete(@Parameter(description = "ID созданной темы для удаления") @PathVariable Long topicId) throws TopicNotFoundException;


}
