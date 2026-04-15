# OIDC Logout Success Handler helper method (Java)

## Insert Point
As a package-private helper method in the configuration class body.
**NOT a @Bean** — called directly from filterChain via `http.logout(logout -> logout.logoutSuccessHandler(oidcClientInitiatedLogoutSuccessHandler()))`.
Requires `clientRegistrationRepository` field injected via constructor.

## Code

```java
// defaults: always generated for OIDC_STATEFUL when isJwt=false

// Constructor-injected field (add to class):
private final org.springframework.security.oauth2.client.registration.ClientRegistrationRepository clientRegistrationRepository;

// Constructor:
public {className}(org.springframework.security.oauth2.client.registration.ClientRegistrationRepository clientRegistrationRepository) {
    this.clientRegistrationRepository = clientRegistrationRepository;
}

// Helper method (NOT @Bean):
org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler oidcClientInitiatedLogoutSuccessHandler() {
    org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler successHandler = new org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler(clientRegistrationRepository);
    successHandler.setPostLogoutRedirectUri("{postLogoutRedirectUri}");
    return successHandler;
}
```

## Variables
| Variable | Source | Default |
|----------|--------|---------|
| `{postLogoutRedirectUri}` | user input | `http://localhost:8080/` |
