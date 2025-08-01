package ua.hudyma.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.view.RedirectView;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Log4j2
public class AuthController {

    @Value("${spring.security.oauth2.client.registration.auth0.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.auth0.client-secret}")
    private String secret;

    @Value("${custom.audience}")
    private String audience;

    @Value("${spring.security.oauth2.client.provider.auth0.issuer-uri}")
    private String issuerUri;

    @Value("${custom.token-uri}")
    private String tokenUri;

    @Value("${custom.redirect-uri}")
    private String redirectUri;

    @Value("${custom.logout-return-to}")
    private String logoutReturnTo;
    @Value("${custom.auth-uri}")
    private String authUri;
    @Value("${custom.callback-uri}")
    private String callbackUri;

    @Value("${custom.auth-string}")
    private String authString;


    @GetMapping("/authorize")
    public RedirectView authorize(HttpServletResponse response) {
        log.info(authString);
        return new RedirectView(authString);
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }


    @GetMapping("/logout")
    public void logout(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication,
            @RequestParam(
                    value = "returnTo", required = false, defaultValue = "/") String returnTo) throws IOException {
        if (authentication != null) {
            new SecurityContextLogoutHandler().logout(request, response, authentication);
        }

        String normalizedIssuer = issuerUri.endsWith("/")
                ? issuerUri.substring(0, issuerUri.length() - 1)
                : issuerUri;
        String logoutUrl = normalizedIssuer +
                "/v2/logout?client_id=" + clientId + "&returnTo=" + logoutReturnTo;
        response.sendRedirect(logoutUrl);
    }

    @GetMapping("/custom-login")
    public void login(HttpServletResponse response) throws IOException {
        response.sendRedirect("/oauth2/authorization/auth0");
    }

    @GetMapping("/callback")
    public ResponseEntity<?> callback(@RequestParam String code) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Map<String, String> body = Map.of(
                "grant_type",
                "authorization_code",
                "client_id", clientId,
                "client_secret", secret,
                "code", code,
                "redirect_uri", redirectUri);
        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);
        ResponseEntity<Map<String, Object>> response = restTemplate
                .exchange(
                        tokenUri,
                        HttpMethod.POST,
                        request,
                        new ParameterizedTypeReference<>() {
        });
        return ResponseEntity.ok(response.getBody());
    }

    @GetMapping("/token")
    public ResponseEntity<?> getToken(
            @RegisteredOAuth2AuthorizedClient("auth0")
            OAuth2AuthorizedClient client,
            @AuthenticationPrincipal OidcUser oidcUser) {
        return ResponseEntity.ok(Map.of(
                "access_token", client.getAccessToken().getTokenValue(),
                "expires_at", client.getAccessToken().getExpiresAt(),
                "user_email", oidcUser.getEmail()));
    }

    @GetMapping("/")
    public String home() {
        return "API is running";
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(
            @AuthenticationPrincipal OidcUser principal) {
        return ResponseEntity.ok(Map.of(
                "name", principal.getFullName(),
                "email", principal.getEmail(),
                "claims", principal.getClaims()));
    }
}
