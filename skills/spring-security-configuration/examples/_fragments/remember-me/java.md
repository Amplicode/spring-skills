# Remember Me DSL fragment (Java)

## Insert Point
Inside filterChain method body, before `return http.build();`

## Code

```java
// defaults: not included (Remember Me is disabled by default)
// Only generated when Remember Me is enabled

// Default (cookie-based):
http.rememberMe(org.springframework.security.config.Customizer.withDefaults());

// With options:
http.rememberMe(rememberMe -> rememberMe
    .key("{rememberMeKey}")                                                       // if key is not blank
    .tokenValiditySeconds({tokenValiditySeconds})                                 // if tokenValiditySeconds is set (default: 1209600 = 2 weeks)
    .rememberMeParameter("{rememberMeParameter}")                                 // if rememberMeParameter is not blank (default: "remember-me")
    .rememberMeCookieName("{rememberMeCookieName}")                               // if rememberMeCookieName is not blank (default: "remember-me")
    .useSecureCookie({useSecureCookie})                                            // if useSecureCookie is explicitly set
    .userDetailsService({userDetailsServiceBean})                                 // if userDetailsService bean is set
    .tokenRepository({tokenRepositoryBean})                                       // if tokenRepository bean is set (for persistent tokens)
    .alwaysRemember({alwaysRemember})                                             // if alwaysRemember is explicitly set (default: false)
    .rememberMeCookieDomain("{cookieDomain}")                                     // if cookieDomain is not blank
    .authenticationSuccessHandler({authSuccessHandlerBean})                       // if authenticationSuccessHandler bean is set
    .rememberMeServices({rememberMeServicesBean})                                 // if rememberMeServices bean is set (overrides default implementation)
);
```

## Variables
| Variable | Source | Default |
|----------|--------|---------|
| `{rememberMeKey}` | user input | skip if empty |
| `{tokenValiditySeconds}` | user input (int) | skip if default (1209600) |
| `{rememberMeParameter}` | user input | skip if default ("remember-me") |
| `{rememberMeCookieName}` | user input | skip if default ("remember-me") |
| `{useSecureCookie}` | user choice (boolean) | skip if not set |
| `{userDetailsServiceBean}` | user input (bean ref) | skip if not set |
| `{tokenRepositoryBean}` | user input (bean ref) | skip if not set |
| `{alwaysRemember}` | user choice (boolean) | skip if default (false) |
| `{cookieDomain}` | user input | skip if empty |
| `{authSuccessHandlerBean}` | user input (bean ref) | skip if not set |
| `{rememberMeServicesBean}` | user input (bean ref) | skip if not set; overrides default |
