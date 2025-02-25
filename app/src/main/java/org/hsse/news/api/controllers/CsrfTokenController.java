package org.hsse.news.api.controllers;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/csrf-token")
public class CsrfTokenController {
    @GetMapping
    public String csrfToken(CsrfToken csrfToken) {
        return csrfToken.getToken();
    }
}
