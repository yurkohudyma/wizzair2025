package ua.hudyma.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

import java.io.IOException;
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

    @GetMapping("/logout")
    public void logout(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication,
            @RequestParam(value = "returnTo", required = false, defaultValue = "/") String returnTo
    ) throws IOException {
        if (authentication != null) {
            new SecurityContextLogoutHandler().logout(request, response, authentication);
        }

        String normalizedIssuer = issuerUri.endsWith("/") ? issuerUri.substring(0, issuerUri.length() - 1) : issuerUri;
        String logoutUrl = normalizedIssuer + "/v2/logout?client_id=" + clientId + "&returnTo=" + logoutReturnTo;
        response.sendRedirect(logoutUrl);
    }

    @GetMapping("/custom-login")
    public void login(HttpServletResponse response) throws IOException {
        log.info("--uri = {}", issuerUri);
        log.info("--clientId = {}", clientId);
        log.info("--audience = {}", audience);
        log.info("--token-uri = {}", tokenUri);
        log.info("--issuer-uri = {}", issuerUri);

     /*   String url = UriComponentsBuilder.fromUriString(authUri)
                .queryParam("response_type", "code")
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("scope", "openid profile email")
                .queryParam("audience", audience)
                .build()
                .toUriString();
        response.sendRedirect("/oauth2/authorization/auth0");*/
        response.sendRedirect("/oauth2/authorization/auth0");

    }

    @GetMapping("/callback")
    public ResponseEntity<?> callback(@RequestParam String code) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> body = Map.of(
                "grant_type", "authorization_code",
                "client_id", clientId,
                "client_secret", secret,
                "code", code,
                "redirect_uri", redirectUri
        );

        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                tokenUri,
                request,
                Map.class
        );

        return ResponseEntity.ok(response.getBody());
    }

    @GetMapping("/token")
    public ResponseEntity<?> getToken(
            @RegisteredOAuth2AuthorizedClient("auth0") OAuth2AuthorizedClient client,
            @AuthenticationPrincipal OidcUser oidcUser
    ) {
        return ResponseEntity.ok(Map.of(
                "access_token", client.getAccessToken().getTokenValue(),
                "expires_at", client.getAccessToken().getExpiresAt(),
                "user_email", oidcUser.getEmail()
        ));
    }


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
