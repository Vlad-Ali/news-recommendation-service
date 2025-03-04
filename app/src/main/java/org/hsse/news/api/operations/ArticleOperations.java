package org.hsse.news.api.operations;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.hsse.news.database.article.models.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequestMapping("/default")
public interface ArticleOperations {

  @GetMapping("/{articleId}")
  @Operation(summary = "Получение статьи по ее ID")
  @ApiResponse(responseCode = "200", description = "Статья найдена")
  ResponseEntity<ArticleData> getArticle(@Parameter(description = "ID статьи") @PathVariable UUID articleId);

  @GetMapping("/user/{userId}")
  @Operation(summary = "Получение статьи по id")
  @ApiResponse(responseCode = "200", description = "Статья найдена")
  ResponseEntity<ArticleListData> getUserArticles(
      @Parameter(description = "ID пользователя") @PathVariable UUID userId);

  @PostMapping("/create")
  @Operation(summary = "Создание статьи")
  @ApiResponse(responseCode = "201", description = "Статья создана")
  ResponseEntity<String> createArticle(@RequestBody ArticleData articleData);

  @PutMapping("/update/{articleId}")
  @Operation(summary = "Обновление статьи")
  @ApiResponse(responseCode = "201", description = "Статья обновлена")
  ResponseEntity<String> updateArticle(
      @Parameter(description = "ID статьи") @PathVariable UUID articleId,
      @RequestBody ArticleData articleData
  );
}