# JWT (OAuth2 Resource Server) Variant

Authentication type: `JWT_STATELESS`

## Fragments Used

1. Common DSL (see `references/common-dsl.md`)
2. JWT (`_fragments/jwt/{lang}.md`)
3. JWT Converter bean (`_beans/jwt-converter/{lang}.md`) -- optional

## Dependencies

- `_dependencies/base.md` (always)
- `_dependencies/resource-server.md` (always for JWT)

## Properties

- `_properties/jwt/properties.md`

## Questions

### Provider Question

```
OAuth2 provider for JWT?
1. No provider (manual setup) (default)
2. Keycloak
3. AWS Cognito
4. Okta
5. Other
```

### JWK Set URI Question

```
JWK Set URI type?
1. Standard Spring property (default) -- spring.security.oauth2.resourceserver.jwt.jwk-set-uri
2. Custom property key -- injection via @Value
```

If SPRING: ask for issuer URI and JWK set URI values.
If CUSTOM: ask for property key name and URI value.

### JWT Converter Question (only for Keycloak/AWS Cognito)

```
Generate JwtAuthenticationConverter for role mapping? [no]
```

If yes: add `_beans/jwt-converter/{lang}.md` to the class body. The converter uses:
- `"roles"` claim for Keycloak
- `"cognito:groups"` claim for AWS Cognito

### JWS Algorithm Question

```
JWT signing algorithm? [RS256]
```

If not RS256, add `jws-algorithms` property.

## Generation

**JWT DSL order: authorizeHttpRequests FIRST, then headers, then oauth2ResourceServer, then anonymous, csrf. No sessionManagement.**

1. Read skeleton from `_skeletons/{lang}.md`
2. Insert `http.authorizeHttpRequests(...)` fragment (FIRST in filterChain)
3. Insert `http.headers(...)` — Customizer.withDefaults() by default
4. Insert JWT fragment from `_fragments/jwt/{lang}.md` — `http.oauth2ResourceServer(...)`
5. Insert `http.anonymous(...)` — Customizer.withDefaults() by default
6. Insert `http.csrf(...)` — Customizer.withDefaults() by default. CSRF is NOT auto-disabled for JWT; it is controlled by the user via the CSRF question in common DSL
7. If generateConverter=true: add converter helper from `_beans/jwt-converter/{lang}.md`, reference it in the JWT DSL via `.jwtAuthenticationConverter(jwtAuthenticationConverter())`
8. **CRITICAL**: The converter method is NOT @Bean — it is a package-private helper method called directly from filterChain
9. If jwkSetUriType=CUSTOM: add @Value field to class for JWK Set URI
10. Add dependencies from `_dependencies/base.md` + `_dependencies/resource-server.md`
11. Write JWT properties from `_properties/jwt/properties.md`
