package ua.hudyma.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class PublicController {
    @GetMapping("/")
    public String home() {
        return "API is running";
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal OidcUser principal) {
        return ResponseEntity.ok(Map.of(
                "name", principal.getFullName(),
                "email", principal.getEmail(),
                "claims", principal.getClaims()
        ));
    }

}
