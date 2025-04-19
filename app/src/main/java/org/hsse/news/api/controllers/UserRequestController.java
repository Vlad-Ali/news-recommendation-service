package org.hsse.news.api.controllers;

import org.hsse.news.api.schemas.request.website.UserUrlRequest;
import org.hsse.news.database.user.models.UserId;
import org.hsse.news.database.userrequest.UserRequestService;
import org.hsse.news.database.userrequest.exception.IncorrectURLException;
import org.hsse.news.database.userrequest.exception.RequestAlreadyExistsException;
import org.hsse.news.database.userrequest.exception.TimeLimitException;
import org.hsse.news.database.userrequest.model.TopUserRequests;
import org.hsse.news.database.userrequest.model.UserRequestDto;
import org.hsse.news.database.userrequest.model.UserRequestUrl;
import org.hsse.news.database.website.exceptions.WebsiteAlreadyExistsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

import java.net.URISyntaxException;
import java.time.Duration;
import java.time.Instant;

@Controller
@RequestMapping("/websites/requests")
public class UserRequestController {

    private static final Logger LOG = LoggerFactory.getLogger(UserRequestController.class);
    private final UserRequestService userRequestService;

    public UserRequestController(final UserRequestService userRequestService) {
        this.userRequestService = userRequestService;
    }

    @PutMapping
    public ResponseEntity<UserRequestDto> createRequest(final @RequestBody UserUrlRequest userUrlRequest){
        final UserId userId = getCurrentUserId();
        final Instant lastTime = userRequestService.getLastRequestByUserId(userId);
        final long days = Duration.between(lastTime, Instant.now()).toDays();
        if (days < 2){
            throw new TimeLimitException("Ваше последнее сообщение было отправлено "+lastTime+". 2 дня с этого момента не прошло");
        }
        final UserRequestDto userRequestDto = userRequestService.addUserRequest(userId, userUrlRequest.url());
        LOG.debug("Successfully created request for user with id = {}", userId);
        return ResponseEntity.ok(userRequestDto);
    }

    @GetMapping("/top")
    public ResponseEntity<TopUserRequests> getTopRequests(){
        final UserId userId = getCurrentUserId();
        final TopUserRequests topUserRequests = userRequestService.getTopRequests();
        LOG.debug("Successfully got top requests");
        return ResponseEntity.ok(topUserRequests);
    }

    @DeleteMapping
    public ResponseEntity<String> deleteRequests(final @RequestBody UserRequestUrl userRequestUrl){
        final UserId userId = getCurrentUserId();
        userRequestService.removeRequestsByUrl(userRequestUrl.url());
        LOG.debug("Successfully removed requests by url = {}", userRequestUrl.url());
        return ResponseEntity.ok("Requests by url = "+ userRequestUrl.url()+ " removed");
    }

    @ExceptionHandler(TimeLimitException.class)
    public ErrorResponse handleTimeLimit(final TimeLimitException ex){
        LOG.debug("Timelimit of sending requests for user: {}", ex.getMessage());
        return ErrorResponse.create(ex, HttpStatus.REQUEST_TIMEOUT, "Time limit");
    }

    @ExceptionHandler(WebsiteAlreadyExistsException.class)
    public ErrorResponse handleWebsiteAlreadyExists(final WebsiteAlreadyExistsException ex){
        LOG.debug("Website already exists: {}", ex.getMessage());
        return ErrorResponse.create(ex, HttpStatus.CONFLICT, "Website already exists");
    }

    @ExceptionHandler(RequestAlreadyExistsException.class)
    public ErrorResponse handleRequestAlreadyExists(final RequestAlreadyExistsException ex){
        LOG.debug("Request for website already exists: {}", ex.getMessage());
        return ErrorResponse.create(ex, HttpStatus.CONFLICT, "Request already exists");
    }

    @ExceptionHandler(IncorrectURLException.class)
    public ErrorResponse handleURISyntax(final URISyntaxException ex){
        LOG.debug("Syntax error at URI: {}", ex.getMessage());
        return ErrorResponse.create(ex, HttpStatus.UNSUPPORTED_MEDIA_TYPE, ex.getMessage());
    }

    private UserId getCurrentUserId() {
        final Object principal = SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        if ("anonymousUser".equals(principal)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "Authorization required");
        }
        return (UserId) principal;
    }
}
