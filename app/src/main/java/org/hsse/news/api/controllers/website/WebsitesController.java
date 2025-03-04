package org.hsse.news.api.controllers.website;

import io.swagger.v3.oas.annotations.Parameter;
import org.hsse.news.api.schemas.request.website.CustomWebsiteCreateRequest;
import org.hsse.news.api.schemas.request.website.SubWebsitesUpdateRequest;
import org.hsse.news.api.schemas.response.website.WebsitesResponse;
import org.hsse.news.api.schemas.shared.WebsiteInfo;
import org.hsse.news.database.user.models.UserId;
import org.hsse.news.database.website.WebsiteService;
import org.hsse.news.database.website.exceptions.QuantityLimitExceededWebsitesPerUserException;
import org.hsse.news.database.website.exceptions.WebsiteAlreadyExistsException;
import org.hsse.news.database.website.exceptions.WebsiteNotFoundException;
import org.hsse.news.database.website.models.Website;
import org.hsse.news.database.website.models.WebsiteId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/websites")
public class WebsitesController implements WebsiteOperations{
  private static final Logger LOG = LoggerFactory.getLogger(WebsitesController.class);
  private static final int MAX_WEBSITES_PER_USER =
      10;
  private final WebsiteService websiteService;

  public WebsitesController(
      final WebsiteService websiteService) {
    this.websiteService = websiteService;
  }

  @Override
  public ResponseEntity<WebsiteInfo> get(
      final @Parameter(description = "ID сайта для получения") @PathVariable Long id) {
    getCurrentUserId();
    final Optional<WebsiteInfo> website = websiteService.findById(new WebsiteId(id));
    if (website.isEmpty()){
      throw new WebsiteNotFoundException(new WebsiteId(id));
    }
    LOG.debug("Website found by id = {} for user ", id);
    return ResponseEntity.ok(website.get());
  }

  @Override
  public ResponseEntity<WebsitesResponse> getUsersWebsites() {
    final UserId userId = getCurrentUserId();
    LOG.debug("Websites found by user with id = {}", userId.value());
    return ResponseEntity.ok(websiteService.getSubAndUnSubWebsites(userId));
  }

  @Override
  public ResponseEntity<String> updateSubWebsites(
      final @Parameter(description = "Данные для обновления выбранных сайтов") @RequestBody
          SubWebsitesUpdateRequest subWebsitesUpdateRequest)
  {
    final UserId userId = getCurrentUserId();
    final List<WebsiteId> websitesIds =
        subWebsitesUpdateRequest.websiteIds().stream().map(WebsiteId::new).toList();
    try {
      websiteService.tryUpdateSubscribedWebsites(websitesIds, userId);
    } catch (QuantityLimitExceededWebsitesPerUserException ex) {
      throw new QuantityLimitExceededWebsitesPerUserException(
          String.format("Chosen websites more than limit = %s", MAX_WEBSITES_PER_USER));
    }
    LOG.debug("Successfully updated subWebsites for user with id = {}", userId.value());
    return ResponseEntity.ok("SubWebsites updated");
  }

  @Override
  public ResponseEntity<WebsiteInfo> create(
      final @Parameter(description = "Данные для создания Сайта пользователем") @RequestBody
          CustomWebsiteCreateRequest customWebsiteCreateRequest)
      {
    final UserId userId = getCurrentUserId();
    final Website website =
        websiteService.create(
            new Website(
                null,
                customWebsiteCreateRequest.url(),
                customWebsiteCreateRequest.description(),
                userId));
    LOG.debug("Successfully created website with id = {}", website.id());
    return ResponseEntity.ok(new WebsiteInfo(website.id().value(), website.url(), website.description()));
  }

  @Override
  public ResponseEntity<String> delete(
      final @Parameter(description = "ID созданного сайта для удаления") @PathVariable Long websiteId) {
    final UserId userId = getCurrentUserId();
    websiteService.delete(new WebsiteId(websiteId), userId);
    LOG.debug("Successfully deleted website with id = {} by user with id = {}", websiteId, userId);
    return ResponseEntity.ok("Website deleted");
  }

  private UserId getCurrentUserId() {
    final Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    if ("anonymousUser".equals(principal)) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authorization required");
    }
    return (UserId) principal;
  }

  @ExceptionHandler(WebsiteNotFoundException.class)
  public ErrorResponse handleWebsiteNotFoundException(final WebsiteNotFoundException ex) {
    LOG.debug("Website not found by id = {}", ex.getMessage());
    return ErrorResponse.create(ex, HttpStatus.BAD_REQUEST, "Website not found");
  }

  @ExceptionHandler(WebsiteAlreadyExistsException.class)
  public ErrorResponse handleWebsiteAlreadyExistsException(final WebsiteAlreadyExistsException ex) {
    LOG.debug("Website already exists: {}", ex.getMessage());
    return ErrorResponse.create(ex, HttpStatus.CONFLICT, "Website already exists");
  }

  @ExceptionHandler(QuantityLimitExceededWebsitesPerUserException.class)
  public ErrorResponse handleQuantityLimitExceededWebsitesPerUserException(
          final QuantityLimitExceededWebsitesPerUserException ex) {
    LOG.debug("Limit of subscribed websites: {}", ex.getMessage());
    return ErrorResponse.create(ex, HttpStatus.LENGTH_REQUIRED, "Limit of chosen websites");
  }

}
