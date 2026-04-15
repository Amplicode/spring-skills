# Logout DSL fragment (Java)

## Insert Point
Inside filterChain method body, before `return http.build();`

## Code

```java
// defaults: enabled by Spring Security automatically
// When all options are default — skip this fragment entirely (Spring Security provides default logout at /logout)

// When disabled=true:
http.logout(logout -> logout.disable());

// With options:
http.logout(logout -> logout
    .logoutUrl("{logoutUrl}")                                                    // if logoutUrl is not blank (default: /logout)
    .logoutRequestMatcher({logoutRequestMatcherBean})                            // if logoutRequestMatcher bean is set (overrides logoutUrl)
    .logoutSuccessUrl("{logoutSuccessUrl}")                                       // if logoutSuccessUrl is not blank (default: /login?logout)
    .clearAuthentication({clearAuthentication})                                   // if clearAuthentication is explicitly set (default: true)
    .invalidateHttpSession({invalidateHttpSession})                               // if invalidateHttpSession is explicitly set (default: true)
    .deleteCookies("{cookie1}", "{cookie2}")                                      // if deleteCookies is not empty
    .logoutSuccessHandler({logoutSuccessHandlerBean})                             // if logoutSuccessHandler bean is set (overrides logoutSuccessUrl)
    .addLogoutHandler({logoutHandlerBean})                                        // for each additional logout handler bean (repeated)
    .permitAll()                                                                  // if logoutUrl is customized
);

// With STATUS handler (uses HttpStatusReturningLogoutSuccessHandler, NOT a lambda):
http.logout(logout -> logout
    .logoutSuccessHandler(new org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler(org.springframework.http.HttpStatus.{logoutSuccessStatus})));

// OIDC variant (used with OAuth2/OIDC Login when isJwt=false):
http.logout(logout -> logout
    .logoutSuccessHandler(oidcClientInitiatedLogoutSuccessHandler())
);
```

## Variables
| Variable | Source | Default |
|----------|--------|---------|
| `{logoutUrl}` | user input | skip if default (`/logout`) |
| `{logoutSuccessUrl}` | user input | skip if default (`/login?logout`) |
| `{clearAuthentication}` | user choice (boolean) | skip if default (true) |
| `{invalidateHttpSession}` | user choice (boolean) | skip if default (true) |
| `{cookie1}`, `{cookie2}` | user input (comma-separated) | skip if empty |
| `{logoutRequestMatcherBean}` | user input (bean ref) | skip if not set; overrides logoutUrl |
| `{logoutSuccessHandlerBean}` | user input (bean ref) | skip if not set |
| `{logoutSuccessStatus}` | user choice (HTTP status) | skip if not set (e.g. `OK`) |
| `{logoutHandlerBean}` | user input (bean ref) | skip if not set; repeated for each handler |
