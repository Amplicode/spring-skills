# OAuth2 Login DSL fragment (Kotlin)

## Insert Point
Inside filterChain method body, before `return http.build()`

## Code

```kotlin
// defaults: oauth2Login with defaults, logout with OIDC handler
// Default output (no customization):
http.oauth2Login(org.springframework.security.config.Customizer.withDefaults())
http.logout { it.logoutSuccessHandler(oidcClientInitiatedLogoutSuccessHandler()) }

// When disabled=true:
http.oauth2Login { it.disable() }

// With full options:
http.oauth2Login { oauth2Login ->
    oauth2Login
        .loginPage("{loginPage}")                                              // if loginPage is not blank
        .failureUrl("{failureUrl}")                                            // if failureUrl is not blank
        .loginProcessingUrl("{loginProcessingUrl}")                            // if loginProcessingUrl is not blank
        .defaultSuccessUrl("{defaultSuccessUrl}")                               // if defaultSuccessUrl is not blank
        .authorizationEndpoint { authorizationEndpoint ->
            authorizationEndpoint
                .baseUri({authorizationEndpointField})                         // if authorizationEndpoint is not blank; extracted to @Value property
                .authorizationRequestResolver({authorizationRequestResolverBean})  // if authorizationRequestResolver bean is set
                .authorizationRequestRepository({authorizationRequestRepoBean})    // if authorizationRequestRepository bean is set
        }
        .redirectionEndpoint { redirectionEndpoint ->
            redirectionEndpoint
                .baseUri({redirectionEndpointField})                           // if redirectionEndpoint is not blank; extracted to @Value property
        }
        .authenticationDetailsSource({authDetailsSourceBean})                   // if authenticationDetailsSource bean is set
        .successHandler({successHandlerBean})                                   // if successHandler bean is set
        .failureHandler({failureHandlerBean})                                   // if failureHandler bean is set
        .clientRegistrationRepository({clientRegRepoBean})                     // if clientRegistrationRepository bean is set
        .authorizedClientRepository({authorizedClientRepoBean})                // if authorizedClientRepository bean is set
        .authorizedClientService({authorizedClientServiceBean})                // if authorizedClientService bean is set
        .tokenEndpoint { tokenEndpoint ->
            tokenEndpoint
                .accessTokenResponseClient({accessTokenResponseClientBean})    // if accessTokenResponseClient bean is set
        }
        .userInfoEndpoint { userInfoEndpoint ->
            userInfoEndpoint
                .userService({userServiceBean})                                // if userService bean is set
                .oidcUserService({oidcUserServiceBean})                        // if oidcUserService bean is set
                .userAuthoritiesMapper({userAuthoritiesMapperBean})             // if userAuthoritiesMapper bean is set
        }
}

// Logout (when !isJwt):
http.logout { it.logoutSuccessHandler(oidcClientInitiatedLogoutSuccessHandler()) }

// When isJwt=true (JWT-based OIDC, no logout handler):
// omit the http.logout() block entirely
```

## Variables
| Variable | Source | Default |
|----------|--------|---------|
| `{loginPage}` | user input | skip if empty |
| `{failureUrl}` | user input | skip if empty |
| `{loginProcessingUrl}` | user input | skip if empty |
| `{defaultSuccessUrl}` | user input | skip if empty |
| `{authorizationEndpointField}` | extracted to `@Value("${appPrefix}.oauth.authorization-endpoint")` field | skip if empty |
| `{redirectionEndpointField}` | extracted to `@Value("${appPrefix}.oauth.redirection-endpoint")` field | skip if empty |
| `{authDetailsSourceBean}` | user input (bean ref) | skip if not set |
| `{successHandlerBean}` | user input (bean ref) | skip if not set |
| `{failureHandlerBean}` | user input (bean ref) | skip if not set |
| `{clientRegRepoBean}` | user input (bean ref) | skip if not set |
| `{authorizedClientRepoBean}` | user input (bean ref) | skip if not set |
| `{authorizedClientServiceBean}` | user input (bean ref) | skip if not set |
| `{accessTokenResponseClientBean}` | user input (bean ref) | skip if not set |
| `{authorizationRequestResolverBean}` | user input (bean ref) | skip if not set |
| `{authorizationRequestRepoBean}` | user input (bean ref) | skip if not set |
| `{userServiceBean}` | user input (bean ref) | skip if not set |
| `{oidcUserServiceBean}` | user input (bean ref) | skip if not set |
| `{userAuthoritiesMapperBean}` | bean method reference | auto-generated for KEYCLOAK (see _beans/role-mapper/) |
| `oidcClientInitiatedLogoutSuccessHandler()` | bean method reference | auto-generated bean (see _beans/logout-handler/) |

## Notes
- `authorizationEndpoint` and `redirectionEndpoint` values are extracted to application properties via `@Value` field injection
- Nested DSL blocks are only emitted if at least one of their sub-options is set
