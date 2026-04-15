# SessionManagement DSL fragment (Kotlin)

## Insert Point
Inside filterChain method body, before `return http.build()`

## Code

```kotlin
// defaults: enabled, sessionFixation=CHANGE_SESSION_ID, sessionCreationPolicy=IF_REQUIRED
// When all options are default — skip this fragment entirely

// When disabled=true:
http.sessionManagement { it.disable() }

// Boot < 3.1, with options:
http.sessionManagement { sessionManagement ->
    sessionManagement
        .sessionFixation { sessionFixation -> sessionFixation.{fixationMethod}() }
        .sessionCreationPolicy(org.springframework.security.config.http.SessionCreationPolicy.{POLICY})
        .invalidSessionUrl("{invalidSessionUrl}")
        .enableSessionUrlRewriting(true)
        .sessionAuthenticationErrorUrl("{authErrorUrl}")
        .maximumSessions({maximumSessions})                                  // ALL below require maximumSessions != null
            .maxSessionsPreventsLogin(true)
            .expiredUrl("{expiredUrl}")
            .expiredSessionStrategy({expiredSessionStrategyBean})
            .sessionRegistry({sessionRegistryBean})
        .sessionAuthenticationStrategy({sessionAuthStrategyBean})
        .invalidSessionStrategy({invalidSessionStrategyBean})
        .sessionAuthenticationFailureHandler({sessionFailureHandlerBean})
}

// Boot >= 3.1, with options:
// IMPORTANT: sessionConcurrency sub-fields are INDEPENDENT of maximumSessions
http.sessionManagement { sessionManagement ->
    sessionManagement
        .sessionFixation { sessionFixation -> sessionFixation.{fixationMethod}() }
        .sessionCreationPolicy(org.springframework.security.config.http.SessionCreationPolicy.{POLICY})
        .invalidSessionUrl("{invalidSessionUrl}")
        .enableSessionUrlRewriting(true)
        .sessionAuthenticationErrorUrl("{authErrorUrl}")
        .sessionConcurrency { sessionConcurrency ->
            sessionConcurrency
                .maximumSessions({maximumSessions})                          // if maximumSessions is set (independent null-check)
                .maxSessionsPreventsLogin(true)                              // if maximumSessionsPreventsLogin is true (independent)
                .expiredUrl("{expiredUrl}")                                   // if expiredUrl is not blank (independent)
                .expiredSessionStrategy({expiredSessionStrategyBean})        // if expiredSessionStrategy is set (independent)
                .sessionRegistry({sessionRegistryBean})                      // if sessionRegistry is set (independent)
        }
        .sessionAuthenticationStrategy({sessionAuthStrategyBean})
        .invalidSessionStrategy({invalidSessionStrategyBean})
        .sessionAuthenticationFailureHandler({sessionFailureHandlerBean})
}
```

## Variables
| Variable | Source | Default |
|----------|--------|---------|
| `{fixationMethod}` | user choice | skip if default; options: `none`, `newSession`, `migrateSession` |
| `{POLICY}` | user choice | skip if default; options: `ALWAYS`, `NEVER`, `STATELESS` |
| `{invalidSessionUrl}` | user input | skip if empty |
| `{authErrorUrl}` | user input | skip if empty |
| `{maximumSessions}` | user input (int) | skip if not set |
| `{expiredUrl}` | user input | skip if empty |
| `{expiredSessionStrategyBean}` | user input (bean ref) | skip if not set |
| `{sessionRegistryBean}` | user input (bean ref) | skip if not set |
| `{sessionAuthStrategyBean}` | user input (bean ref) | skip if not set |
| `{invalidSessionStrategyBean}` | user input (bean ref) | skip if not set |
| `{sessionFailureHandlerBean}` | user input (bean ref) | skip if not set |
