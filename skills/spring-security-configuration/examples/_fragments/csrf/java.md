# CSRF DSL fragment

## Insert Point
Inside filterChain method body, before `return http.build();`

## Code

```java
// defaults: enabled, no matchers
// When all options are default — skip this fragment entirely

// When disabled=true:
http.csrf(csrf -> csrf.disable());

// Boot 3, with options:
http.csrf(csrf -> csrf
    .ignoringRequestMatchers("{pattern1}", "{pattern2}")                    // if ignoringMatchers not empty
    .csrfTokenRepository({csrfTokenRepositoryBean})                        // if csrfTokenRepository is set
    .requireCsrfProtectionMatcher({requireCsrfProtectionMatcherBean})      // if requireCsrfProtectionMatcher is set
    .sessionAuthenticationStrategy({sessionAuthenticationStrategyBean})     // if sessionAuthenticationStrategy is set
);
```

## Variables
| Variable | Source | Default |
|----------|--------|---------|
| `{pattern1}`, `{pattern2}` | user input | — |
| `{csrfTokenRepositoryBean}` | user input (bean ref) | skip if not set |
| `{requireCsrfProtectionMatcherBean}` | user input (bean ref) | skip if not set |
| `{sessionAuthenticationStrategyBean}` | user input (bean ref) | skip if not set |
