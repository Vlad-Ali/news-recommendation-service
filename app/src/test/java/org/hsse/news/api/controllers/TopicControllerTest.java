package org.hsse.news.api.controllers;

import org.hsse.news.api.util.SimpleHttpClient;
import org.hsse.news.database.topic.TopicService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class TopicControllerTest {
    private SimpleHttpClient client;
    private String baseUrl;

    @Mock
    private TopicService topicService;

    @Test
    void should501OnGet() throws IOException, InterruptedException {
        final HttpResponse<String> response = client.get(baseUrl);

        assertEquals(501, response.statusCode(), "should be 501 (Not Implemented)"); // NOPMD - suppressed AvoidDuplicateLiterals - TODO temporal behaviour
    }

    @Test
    void should501OnPut() throws IOException, InterruptedException {
        final HttpResponse<String> response = client.put(
                baseUrl,
                """
                     [
                         42,
                         69,
                         228
                     ]
                     """
        );

        assertEquals(501, response.statusCode(), "should be 501 (Not Implemented)"); // NOPMD - suppressed AvoidDuplicateLiterals - TODO temporal behaviour
    }

    @Test
    void should501OnCreateCustom() throws IOException, InterruptedException {
        final HttpResponse<String> response = client.post(
                baseUrl + "/custom",
                """
                        {
                            "name": "New Topic"
                        }
                     """
        );

        assertEquals(501, response.statusCode(), "should be 501 (Not Implemented)"); // NOPMD - suppressed AvoidDuplicateLiterals - TODO temporal behaviour
    }

    @Test
    void should501OnDeleteCustom() throws IOException, InterruptedException {
        final HttpResponse<String> response = client.delete(baseUrl + "/custom/" + 42);

        assertEquals(501, response.statusCode(), "should be 501 (Not Implemented)"); // NOPMD - suppressed AvoidDuplicateLiterals - TODO temporal behaviour
    }
}
