# Authorize HTTP Requests DSL fragment (Java)

## Insert Point
Inside filterChain method body. Position depends on variant:
- **HTTP Session / JWT / Custom**: FIRST in the DSL chain
- **OAuth2/OIDC**: AFTER `oauth2Login` and `logout` DSL blocks

## Code

```java
// defaults: anyRequest().authenticated()
// Default output:
http.authorizeHttpRequests(authorizeHttpRequests -> authorizeHttpRequests
    .anyRequest().authenticated()
);

// With securityMatcher:
http.securityMatcher("{securityMatcher}");
http.authorizeHttpRequests(authorizeHttpRequests -> authorizeHttpRequests
    .requestMatchers("{pattern1}", "{pattern2}").hasRole("{role}")
    .requestMatchers(org.springframework.http.HttpMethod.{METHOD}, "{pattern}").permitAll()
    .anyRequest().authenticated()
);
```

Permission methods available:
- `.permitAll()` — allow all
- `.authenticated()` — require authentication
- `.hasRole("{role}")` — single role (without ROLE_ prefix)
- `.hasAnyRole("{role1}", "{role2}")` — multiple roles
- `.hasAuthority("{authority}")` — single authority
- `.hasAnyAuthority("{a1}", "{a2}")` — multiple authorities
- `.denyAll()` — deny all
- `.anonymous()` — anonymous only
- `.rememberMe()` — remember-me only
- `.fullyAuthenticated()` — fully authenticated only

Matcher types:
- `.requestMatchers("{pattern}")` — Boot 3+ (default)
- `.requestMatchers(org.springframework.http.HttpMethod.{METHOD}, "{pattern}")` — with HTTP method
- `.dispatcherTypeMatchers(jakarta.servlet.DispatcherType.{TYPE})` — dispatcher type matcher (FORWARD, INCLUDE, REQUEST, ASYNC, ERROR)

## Variables
| Variable | Source | Default |
|----------|--------|---------|
| `{securityMatcher}` | user input | skip if empty |
| `{pattern1}`, `{pattern2}`, `{pattern}` | user input | — |
| `{role}`, `{role1}`, `{role2}` | user input or from `list_spring_security_roles` | — |
| `{authority}`, `{a1}`, `{a2}` | user input | — |
| `{METHOD}` | user choice | `GET`, `POST`, `PUT`, `DELETE`, etc. |
| `{TYPE}` | user choice | `FORWARD`, `INCLUDE`, `REQUEST`, `ASYNC`, `ERROR` |
