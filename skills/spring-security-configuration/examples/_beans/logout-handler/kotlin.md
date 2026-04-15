# OIDC Logout Success Handler helper method (Kotlin)

## Insert Point
As a private/internal helper method in the configuration class body.
**NOT a @Bean** — called directly from filterChain via `http.logout { it.logoutSuccessHandler(oidcClientInitiatedLogoutSuccessHandler()) }`.
Requires `clientRegistrationRepository` injected via constructor.

## Code

```kotlin
// defaults: always generated for OIDC_STATEFUL when isJwt=false

// Constructor-injected field (add to class):
private val clientRegistrationRepository: org.springframework.security.oauth2.client.registration.ClientRegistrationRepository

// Constructor:
// class {className}(private val clientRegistrationRepository: ClientRegistrationRepository) {

// NOT @Bean — helper method called directly from filterChain
fun oidcClientInitiatedLogoutSuccessHandler(): org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler {
    val successHandler: org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler = org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler(clientRegistrationRepository)
    successHandler.setPostLogoutRedirectUri("{postLogoutRedirectUri}")
    return successHandler
}
```

## Variables
| Variable | Source | Default |
|----------|--------|---------|
| `{postLogoutRedirectUri}` | user input | `http://localhost:8080/` |
