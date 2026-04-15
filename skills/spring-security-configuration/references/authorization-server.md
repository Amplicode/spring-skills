# Authorization Server Variant

Authentication type: `OAUTH_AUTHORIZATION_SERVER`
Requires: Spring Boot >= 3.1

## Fragments Used

1. Common DSL (see `references/common-dsl.md`) — all blocks (headers, anonymous, csrf always generated)

## Dependencies

- `_dependencies/base.md` (always)
- `_dependencies/authorization-server.md`

## Properties

- `_properties/authorization-server/properties.md`

## Questions

### Issuer Question

```
Authorization server Issuer URI? [empty]
```

### Endpoint Questions (batch, skip if "all defaults")

```
Endpoint settings (leave empty for defaults):
1. **Authorization URI?** [/oauth2/authorize]
2. **JWK Set URI?** [/oauth2/jwks]
3. **Token URI?** [/oauth2/token]
4. **Token Revocation URI?** [/oauth2/revoke]
5. **Token Introspection URI?** [/oauth2/introspect]
6. **Device Authorization URI?** [/oauth2/device_authorization]
7. **Device Verification URI?** [/oauth2/device_verification]
8. **Logout URI?** [/connect/logout]
9. **Client Registration URI?** [/connect/register]
10. **User Info URI?** [/userinfo]
```

### Client Questions (repeat per client)

```
Add client? [yes/no]
```

If yes:
```
Client settings:
1. **Client name?** []
2. **Client ID?** []
3. **Client Secret?** []
4. **Encode secret (bcrypt)?** [yes]
5. **Authentication methods?** [client_secret_basic] (comma-separated)
6. **Grant types?** [authorization_code] (comma-separated)
7. **Redirect URIs?** [] (comma-separated)
8. **Scopes?** [openid] (comma-separated)
```

### Client Advanced Questions (batch, skip if "all defaults")

```
Advanced client settings {clientName}:
1. **Post-logout redirect URIs?** [empty] (comma-separated)
2. **Require Proof Key (PKCE)?** [no]
3. **Require Authorization Consent?** [no]
```

### Token Configuration Questions (batch, skip if "all defaults")

```
Token settings for client {clientName}:
1. **Authorization code TTL (seconds)?** [300]
2. **Access token TTL (seconds)?** [300]
3. **Device code TTL (seconds)?** [300]
4. **Refresh token TTL (seconds)?** [3600]
5. **Reuse refresh tokens?** [yes]
6. **Token signing algorithm?** [RS256] (maps to token-endpoint-authentication-signing-algorithm, NOT id-token-signature-algorithm)
```

## Generation

**Auth Server DSL order: authorizeHttpRequests → headers → anonymous → csrf** (same as Custom)

1. Read skeleton from `_skeletons/{lang}.md`
2. Insert `http.authorizeHttpRequests(...)` fragment (FIRST in filterChain)
3. Insert `http.headers(Customizer.withDefaults())`
4. Insert `http.anonymous(Customizer.withDefaults())`
5. Insert `http.csrf(Customizer.withDefaults())`
6. Add dependencies from `_dependencies/base.md` + `_dependencies/authorization-server.md`
7. Write server endpoint properties from `_properties/authorization-server/properties.md` (only non-default values)
8. Write client properties for each client (including advanced options: post-logout redirect URIs, require proof key, require authorization consent, device code TTL)

## Notes

- **NO `@Import` annotation** — Spring Boot autoconfigures the Authorization Server via the `spring-boot-starter-security-oauth2-authorization-server` dependency. Do NOT add `@Import(OAuth2AuthorizationServerConfiguration.class)`.
- The Java configuration is identical to the Custom variant — just common DSL fragments. All Authorization Server-specific settings go through `application.properties`.
- Endpoint properties are only written when different from defaults.
- Client secret uses `{bcrypt}` prefix when "Encode" checkbox is selected, `{noop}` otherwise.
- Token TTL properties are only written when different from defaults (auth code: 300s, access: 300s, device: 300s, refresh: 3600s).
