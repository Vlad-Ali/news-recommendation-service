package org.hsse.news.api.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/csrf-token")
@Tag(name="CSRF token API", description = "Получение токена CSRF. " +
        "Его нужно предоставлять в каждом запросе в заголовке X-CSRF-TOKEN")
public class CsrfTokenController {
    @GetMapping
    @Operation(summary = "Получить CSRF токен")
    @ApiResponse(responseCode = "200", description = "Новый CSRF токен")
    public String csrfToken(CsrfToken csrfToken) {
        return csrfToken.getToken();
    }
}
