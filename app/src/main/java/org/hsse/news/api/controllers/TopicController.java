package org.hsse.news.api.controllers;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hsse.news.api.schemas.request.topic.CreateCustomTopicRequest;
import org.hsse.news.database.topic.TopicService;
import org.hsse.news.database.topic.models.Topic;
import org.hsse.news.database.topic.models.TopicId;
import org.springframework.http.HttpStatus;
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
    public List<Topic> get() {
        log.error("Get not implemented");
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Get not implemented");
    }

    @GetMapping("/{id}")
    public Topic get(@PathVariable @Parameter(description = "ID пользователя") final TopicId unusedId) {
        log.error("Get by id not implemented");
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Get by id not implemented");
    }

    @PostMapping
    public void create(@RequestBody final CreateCustomTopicRequest unusedRequest) {
        log.error("Create not implemented");
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Create not implemented");
    }

    @PutMapping("/{id}")
    public void put(@PathVariable @Parameter(description = "ID пользователя") final TopicId unusedId,
                     @RequestBody final CreateCustomTopicRequest unusedRequest) {
        log.error("Put not implemented");
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Put not implemented");
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable @Parameter(description = "ID пользователя") final TopicId unusedId) {
        log.error("Delete not implemented");
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Delete not implemented");
    }
}
