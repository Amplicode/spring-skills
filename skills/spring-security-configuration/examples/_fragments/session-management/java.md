# SessionManagement DSL fragment (Java)

## Insert Point
Inside filterChain method body, before `return http.build();`

## Code

```java
// defaults: enabled, sessionFixation=CHANGE_SESSION_ID, sessionCreationPolicy=IF_REQUIRED
// When all options are default — skip this fragment entirely

// When disabled=true:
http.sessionManagement(sessionManagement -> sessionManagement.disable());

// Boot < 3.1, with options:
http.sessionManagement(sessionManagement -> sessionManagement
    .sessionFixation(sessionFixation -> sessionFixation.{fixationMethod}())  // if sessionFixation is not CHANGE_SESSION_ID; options: none, newSession, migrateSession
    .sessionCreationPolicy(org.springframework.security.config.http.SessionCreationPolicy.{POLICY})  // if sessionCreationPolicy is not IF_REQUIRED; options: ALWAYS, NEVER, STATELESS
    .invalidSessionUrl("{invalidSessionUrl}")                                // if invalidSessionUrl is not blank
    .enableSessionUrlRewriting(true)                                          // if enableSessionUrlRewriting is true
    .sessionAuthenticationErrorUrl("{authErrorUrl}")                          // if authErrorUrl is not blank
    .maximumSessions({maximumSessions})                                      // ALL below require maximumSessions != null
        .maxSessionsPreventsLogin(true)                                      // if maximumSessionsPreventsLogin is true
        .expiredUrl("{expiredUrl}")                                           // if expiredUrl is not blank
        .expiredSessionStrategy({expiredSessionStrategyBean})                // if expiredSessionStrategy is set
        .sessionRegistry({sessionRegistryBean})                              // if sessionRegistry is set
    .sessionAuthenticationStrategy({sessionAuthStrategyBean})                // if sessionAuthenticationStrategy is set
    .invalidSessionStrategy({invalidSessionStrategyBean})                    // if invalidSessionStrategy is set
    .sessionAuthenticationFailureHandler({sessionFailureHandlerBean})         // if sessionFailureHandler is set
);

// Boot >= 3.1, with options:
// IMPORTANT: sessionConcurrency sub-fields are INDEPENDENT of maximumSessions
http.sessionManagement(sessionManagement -> sessionManagement
    .sessionFixation(sessionFixation -> sessionFixation.{fixationMethod}())  // if sessionFixation is not CHANGE_SESSION_ID
    .sessionCreationPolicy(org.springframework.security.config.http.SessionCreationPolicy.{POLICY})  // if sessionCreationPolicy is not IF_REQUIRED
    .invalidSessionUrl("{invalidSessionUrl}")                                // if invalidSessionUrl is not blank
    .enableSessionUrlRewriting(true)                                          // if enableSessionUrlRewriting is true
    .sessionAuthenticationErrorUrl("{authErrorUrl}")                          // if authErrorUrl is not blank
    .sessionConcurrency(sessionConcurrency -> sessionConcurrency
        .maximumSessions({maximumSessions})                                  // if maximumSessions is set (independent null-check)
        .maxSessionsPreventsLogin(true)                                      // if maximumSessionsPreventsLogin is true (independent)
        .expiredUrl("{expiredUrl}")                                           // if expiredUrl is not blank (independent)
        .expiredSessionStrategy({expiredSessionStrategyBean})                // if expiredSessionStrategy is set (independent)
        .sessionRegistry({sessionRegistryBean})                              // if sessionRegistry is set (independent)
    )
    .sessionAuthenticationStrategy({sessionAuthStrategyBean})                // if sessionAuthenticationStrategy is set
    .invalidSessionStrategy({invalidSessionStrategyBean})                    // if invalidSessionStrategy is set
    .sessionAuthenticationFailureHandler({sessionFailureHandlerBean})         // if sessionFailureHandler is set
);
```

## Variables
| Variable | Source | Default |
|----------|--------|---------|
| `{fixationMethod}` | user choice | skip if default (`changeSessionId`); options: `none`, `newSession`, `migrateSession` |
| `{POLICY}` | user choice | skip if default (`IF_REQUIRED`); options: `ALWAYS`, `NEVER`, `STATELESS` |
| `{invalidSessionUrl}` | user input | skip if empty |
| `{authErrorUrl}` | user input | skip if empty |
| `{maximumSessions}` | user input (int) | skip if not set |
| `{expiredUrl}` | user input | skip if empty |
| `{expiredSessionStrategyBean}` | user input (bean ref) | skip if not set |
| `{sessionRegistryBean}` | user input (bean ref) | skip if not set |
| `{sessionAuthStrategyBean}` | user input (bean ref) | skip if not set |
| `{invalidSessionStrategyBean}` | user input (bean ref) | skip if not set |
| `{sessionFailureHandlerBean}` | user input (bean ref) | skip if not set |
