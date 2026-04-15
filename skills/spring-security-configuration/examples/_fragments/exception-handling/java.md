# Exception Handling (Access Denied) DSL fragment (Java)

## Insert Point
Inside filterChain method body, before `return http.build();`

## Code

```java
// defaults (when Access Denied Handling is enabled but no custom settings):
http.exceptionHandling(Customizer.withDefaults());

// When disabled=true:
http.exceptionHandling(exceptionHandling -> exceptionHandling.disable());

// With accessDeniedPage:
http.exceptionHandling(exceptionHandling -> exceptionHandling
    .accessDeniedPage("{accessDeniedPage}")                                       // if accessDeniedPage is not blank
);

// With accessDeniedHandler bean:
http.exceptionHandling(exceptionHandling -> exceptionHandling
    .accessDeniedHandler({accessDeniedHandlerBean})                               // if accessDeniedHandler bean is set
);

// With both accessDeniedHandler and authenticationEntryPoint (bean):
http.exceptionHandling(exceptionHandling -> exceptionHandling
    .accessDeniedHandler({accessDeniedHandlerBean})                               // if accessDeniedHandler bean is set
    .authenticationEntryPoint({authenticationEntryPointBean})                      // if authenticationEntryPoint bean is set
);

// With authenticationEntryPoint as HTTP status (HttpStatusEntryPoint):
http.exceptionHandling(exceptionHandling -> exceptionHandling
    .authenticationEntryPoint(new org.springframework.security.web.authentication.HttpStatusEntryPoint(org.springframework.http.HttpStatus.{ENTRY_POINT_STATUS})) // if authenticationEntryPoint HTTP status is set
);
```

## Variables
| Variable | Source | Default |
|----------|--------|---------|
| `{accessDeniedPage}` | user input | skip if empty |
| `{accessDeniedHandlerBean}` | user input (bean ref) | skip if not set |
| `{authenticationEntryPointBean}` | user input (bean ref) | skip if not set |
| `{ENTRY_POINT_STATUS}` | user choice (HTTP status) | skip if not set (e.g., UNAUTHORIZED, FORBIDDEN) |
