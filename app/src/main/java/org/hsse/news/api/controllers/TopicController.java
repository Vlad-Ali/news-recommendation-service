package org.hsse.news.api.controllers;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hsse.news.api.schemas.request.topic.CreateCustomTopicRequest;
import org.hsse.news.database.topic.TopicService;
import org.hsse.news.database.topic.models.Topic;
import org.hsse.news.database.topic.models.TopicId;
import org.hsse.news.database.user.models.UserId;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/topics")
@AllArgsConstructor
@Slf4j
@Tag(name = "Topic API", description = "Управление топиками")
public class TopicController {
    private final TopicService topicService;

    @GetMapping
    private List<Topic> get() {
        final UserId userId = getCurrentUserId();
        log.error("Get not implemented");
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Not implemented");
    }

    @GetMapping("/{id}")
    private Topic get(@PathVariable @Parameter(description = "ID пользователя") TopicId id) {
        final UserId userId = getCurrentUserId();
        log.error("Get by id not implemented");
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Not implemented");
    }

    @PostMapping
    private void create(@RequestBody CreateCustomTopicRequest request) {
        final UserId userId = getCurrentUserId();
        log.error("Create not implemented");
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Not implemented");
    }

    @PutMapping("/{id}")
    private void put(@PathVariable @Parameter(description = "ID пользователя") TopicId id,
                     @RequestBody CreateCustomTopicRequest request) {
        final UserId userId = getCurrentUserId();
        log.error("Put not implemented");
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Not implemented");
    }

    @DeleteMapping("/{id}")
    private void delete(@PathVariable @Parameter(description = "ID пользователя") TopicId id) {
        final UserId userId = getCurrentUserId();
        log.error("Delete not implemented");
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Not implemented");
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
