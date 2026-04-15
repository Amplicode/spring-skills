# OIDC (OAuth2 Login) Variant

Authentication type: `OIDC_STATEFUL`

## Fragments Used

1. Common DSL (see `references/common-dsl.md`)
2. OAuth2 Login (`_fragments/oauth2-login/{lang}.md`)
3. Logout Handler bean (`_beans/logout-handler/{lang}.md`) -- when isJwt=false
4. Role Mapper bean (`_beans/role-mapper/{lang}.md`) -- always for OIDC

## Dependencies

- `_dependencies/base.md` (always)
- `_dependencies/oauth-client.md` (always for OIDC)

## Properties

- `_properties/oauth/properties.md`

## Questions

### Provider Question

```
OAuth2/OIDC provider?
1. Keycloak
2. Google
3. GitHub
4. Facebook
5. Okta
6. AWS Cognito
7. Other (custom)
```

### Provider Settings (batch)

```
Provider settings {providerName}:
1. **Client ID?** []
2. **Client Secret?** []
3. **Issuer URI?** [] (only for non-predefined providers)
```

For predefined providers (Google, GitHub, Facebook, Okta), only client-id and client-secret are needed.
For custom/non-predefined (Keycloak, AWS Cognito, Custom), also ask for provider URIs.

### Post-Logout Redirect URI Question (only if isJwt=false)

```
Post-logout redirect URI? [http://localhost:8080/]
```

### JWT-based OIDC Question

```
Use JWT tokens (no sessions)? [no]
```

If yes (`isJwt=true`): skip logout handler generation, no `http.logout(...)` DSL.
If no (`isJwt=false`): generate logout handler bean + `http.logout(...)` DSL + autowire `ClientRegistrationRepository`.

## Generation

**OIDC DSL order is DIFFERENT from other variants — oauth2Login comes FIRST, NOT authorizeHttpRequests!**

1. Read skeleton from `_skeletons/{lang}.md`
2. If isJwt=false: add `ClientRegistrationRepository` field + constructor injection (needed for OIDC logout handler)
3. Insert `http.oauth2Login(...)` fragment FIRST (with userInfoEndpoint.userAuthoritiesMapper if Keycloak)
4. If isJwt=false: insert `http.logout(...)` with OIDC handler — SECOND in chain
5. Insert `http.authorizeHttpRequests(...)` — THIRD (NOT first!)
6. Insert common DSL fragments (headers, anonymous, csrf) — only if non-default
7. Add `oidcClientInitiatedLogoutSuccessHandler()` — as package-private helper method (NOT @Bean), called from http.logout()
8. Add `userAuthoritiesMapper()` — for Keycloak: as public method (NOT @Bean), called from oauth2Login DSL; for generic providers: as @Bean (see role-mapper bean file)
9. **CRITICAL**: `oidcClientInitiatedLogoutSuccessHandler()` is NOT a @Bean — it is a helper method called directly from filterChain DSL. `userAuthoritiesMapper()` is NOT @Bean for Keycloak (called from DSL), but IS @Bean for generic providers (registered as Spring bean).
10. Add dependencies from `_dependencies/base.md` + `_dependencies/oauth-client.md`
11. Write OAuth properties from `_properties/oauth/properties.md`
